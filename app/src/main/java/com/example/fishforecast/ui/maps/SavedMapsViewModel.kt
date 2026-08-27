package com.example.fishforecast.ui.maps

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.data.repository.FishingSpotRepository
import com.example.fishforecast.data.repository.RegionPackRepository
import com.example.fishforecast.data.repository.KnowledgeRepository
import com.example.fishforecast.data.repository.OfflineMapRepository
import com.example.fishforecast.domain.knowledge.WaterBodyType
import kotlinx.coroutines.flow.map
import com.example.fishforecast.domain.share.GpxParser
import com.example.fishforecast.domain.share.GpxWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Что показать пользователю после обмена файлами. */
sealed interface SavedMapsMessage {
    data class Info(val text: String) : SavedMapsMessage
    data class Error(val text: String) : SavedMapsMessage
    /** Файл готов к отправке — экран открывает системный выбор приложения. */
    data class PackReady(val file: File) : SavedMapsMessage
}

@HiltViewModel
class SavedMapsViewModel @Inject constructor(
    private val regionPackRepository: RegionPackRepository,
    private val spotRepository: FishingSpotRepository,
    private val fishRepository: FishRepository,
    private val fishingContext: FishingContextRepository,
    private val offlineMapRepository: OfflineMapRepository,
    private val knowledge: KnowledgeRepository
) : ViewModel() {

    val maps: StateFlow<List<SavedMapEntity>> = fishingContext.savedMaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMap: StateFlow<SavedMapEntity?> = fishingContext.activeMap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val spots: StateFlow<List<FishingSpotEntity>> = spotRepository.spots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _busy = mutableStateOf(false)
    val busy: State<Boolean> = _busy

    /**
     * Результат обмена файлами — одноразовое событие, а не состояние:
     * пока пользователь выбирает файл, экран успевает пересобраться, и
     * сообщение-состояние терялось бы, не дойдя до снекбара.
     */
    private val _events = Channel<SavedMapsMessage>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun selectMap(map: SavedMapEntity) {
        viewModelScope.launch { fishingContext.setActiveMap(map.id) }
    }

    fun renameMap(map: SavedMapEntity, name: String) {
        viewModelScope.launch { fishingContext.renameMap(map.id, name.ifBlank { map.name }) }
    }

    /** Типы водоёмов из словаря знаний: рыболов выбирает свой. */
    val waterBodyTypes: StateFlow<List<WaterBodyType>> = knowledge.catalog
        .map { it.waterbodies }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setWaterBodyType(map: SavedMapEntity, type: String?) {
        viewModelScope.launch { fishingContext.setWaterBodyType(map.id, type) }
    }

    fun setDepths(map: SavedMapEntity, shallowM: Double?, deepM: Double?) {
        viewModelScope.launch { fishingContext.setDepths(map.id, shallowM, deepM) }
    }

    /**
     * Пересчёт нормы по истории наблюдений. Нужен, когда рыболов стёр своё
     * значение или хочет обновить расчёт: обычно норма считается один раз
     * сама и больше не трогается.
     */
    fun recalculateNormalPressure(map: SavedMapEntity) {
        viewModelScope.launch {
            fishingContext.refreshNormalPressure(map.id).fold(
                onSuccess = {
                    _events.send(
                        SavedMapsMessage.Info("Норма по наблюдениям: ${it.toInt()} мм рт. ст.")
                    )
                },
                onFailure = {
                    _events.send(SavedMapsMessage.Error("Не удалось посчитать норму: нужна сеть"))
                }
            )
        }
    }

    /** Замер термометром: факт всегда главнее расчёта. */
    fun measureWater(map: SavedMapEntity, temperatureC: Double?) {
        viewModelScope.launch { fishingContext.measureWater(map.id, temperatureC) }
    }

    fun deleteMap(map: SavedMapEntity) {
        viewModelScope.launch { offlineMapRepository.deleteRegion(map) }
    }

    /** Собирает пакет района: знание о месте, а не файл тайлов. */
    fun exportRegion(map: SavedMapEntity) {
        withBusy {
            regionPackRepository.exportRegion(map.id).fold(
                onSuccess = { SavedMapsMessage.PackReady(it) },
                onFailure = { SavedMapsMessage.Error("Не удалось собрать пакет: ${it.message}") }
            )
        }
    }

    fun importPack(uri: Uri) {
        withBusy {
            regionPackRepository.importPack(uri).fold(
                onSuccess = { summary -> SavedMapsMessage.Info(summary.describe()) },
                onFailure = { SavedMapsMessage.Error("Не удалось открыть пакет: ${it.message}") }
            )
        }
    }

    fun importSpots(uri: Uri, openStream: (Uri) -> java.io.InputStream?) {
        withBusy {
            runCatching {
                val parsed = withContext(Dispatchers.IO) {
                    val stream = openStream(uri) ?: error("Не удалось прочитать файл")
                    stream.use(GpxParser::parse)
                }
                parsed.forEach { spotRepository.addSpot(it) }
                parsed.size
            }.fold(
                onSuccess = { count ->
                    if (count > 0) {
                        SavedMapsMessage.Info("Загружено точек: $count")
                    } else {
                        SavedMapsMessage.Info("В файле нет точек с координатами")
                    }
                },
                // Текст ошибки парсера («Unexpected token…») рыболову ничего
                // не объясняет, поэтому наружу идёт понятная причина.
                onFailure = { SavedMapsMessage.Error("Не удалось прочитать GPX: файл повреждён или это не GPX") }
            )
        }
    }

    /** Готовит GPX всех точек; экран отдаёт его системному «Поделиться». */
    suspend fun buildSpotsGpx(): String {
        val fishNames = fishRepository.getAllFish().first().associate { it.id to it.name }
        return GpxWriter.write(spots.value, fishNames)
    }

    private fun withBusy(block: suspend () -> SavedMapsMessage) {
        viewModelScope.launch {
            _busy.value = true
            _events.send(block())
            _busy.value = false
        }
    }
}

/**
 * Короткий отчёт о том, что приехало. Рыболову важно понимать, что он
 * получил не картинку, а знание о месте — и что тайлы надо будет докачать.
 */
private fun RegionPackRepository.ImportSummary.describe(): String = buildString {
    append(if (regionAdded) "Район «$regionName» добавлен" else "Район «$regionName» обновлён")
    val parts = listOfNotNull(
        zonesAdded.takeIf { it > 0 }?.let { "зон: $it" },
        sectorsAdded.takeIf { it > 0 }?.let { "секторов: $it" },
        spotsAdded.takeIf { it > 0 }?.let { "точек: $it" },
        fishAdded.takeIf { it > 0 }?.let { "видов рыб: $it" }
    )
    if (parts.isNotEmpty()) append(" · ${parts.joinToString(", ")}")
    if (needsTiles) append(". Откройте карту при сети — тайлы скачаются")
}
