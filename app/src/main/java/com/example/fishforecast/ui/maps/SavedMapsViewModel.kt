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
import com.example.fishforecast.data.repository.MapLibraryRepository
import com.example.fishforecast.data.repository.OfflineMapRepository
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
    data class MapsReady(val file: File) : SavedMapsMessage
}

@HiltViewModel
class SavedMapsViewModel @Inject constructor(
    private val mapLibraryRepository: MapLibraryRepository,
    private val spotRepository: FishingSpotRepository,
    private val fishRepository: FishRepository,
    private val fishingContext: FishingContextRepository,
    private val offlineMapRepository: OfflineMapRepository
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

    fun setNormalPressure(map: SavedMapEntity, value: Double?) {
        viewModelScope.launch { fishingContext.setNormalPressure(map.id, value) }
    }

    fun setDepths(map: SavedMapEntity, shallowM: Double?, deepM: Double?) {
        viewModelScope.launch { fishingContext.setDepths(map.id, shallowM, deepM) }
    }

    /** Замер термометром: факт всегда главнее расчёта. */
    fun measureWater(map: SavedMapEntity, temperatureC: Double?) {
        viewModelScope.launch { fishingContext.measureWater(map.id, temperatureC) }
    }

    fun deleteMap(map: SavedMapEntity) {
        viewModelScope.launch { offlineMapRepository.deleteRegion(map) }
    }

    fun exportMaps() {
        withBusy {
            mapLibraryRepository.exportMaps().fold(
                onSuccess = { SavedMapsMessage.MapsReady(it) },
                onFailure = { SavedMapsMessage.Error("Не удалось выгрузить карты: ${it.message}") }
            )
        }
    }

    fun importMaps(uri: Uri) {
        withBusy {
            mapLibraryRepository.importMaps(uri).fold(
                onSuccess = { added ->
                    if (added > 0) {
                        SavedMapsMessage.Info("Добавлено карт: $added")
                    } else {
                        SavedMapsMessage.Info("Новых карт в файле не нашлось")
                    }
                },
                onFailure = { SavedMapsMessage.Error("Не удалось загрузить карты: ${it.message}") }
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
