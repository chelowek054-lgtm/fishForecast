package com.example.fishforecast.ui.library

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.MapRegionEntity
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingSpotRepository
import com.example.fishforecast.data.repository.MapLibraryRepository
import com.example.fishforecast.data.repository.OfflineMapRepository
import com.example.fishforecast.domain.share.GpxParser
import com.example.fishforecast.domain.share.GpxWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Что показать пользователю после обмена файлами. */
sealed interface LibraryMessage {
    data class Info(val text: String) : LibraryMessage
    data class Error(val text: String) : LibraryMessage
    /** Файл готов к отправке — экран открывает системный выбор приложения. */
    data class MapsReady(val file: File) : LibraryMessage
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mapLibraryRepository: MapLibraryRepository,
    private val spotRepository: FishingSpotRepository,
    private val fishRepository: FishRepository,
    offlineMapRepository: OfflineMapRepository
) : ViewModel() {

    val regions: StateFlow<List<MapRegionEntity>> = offlineMapRepository.regions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spots: StateFlow<List<FishingSpotEntity>> = spotRepository.spots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _busy = mutableStateOf(false)
    val busy: State<Boolean> = _busy

    private val _message = mutableStateOf<LibraryMessage?>(null)
    val message: State<LibraryMessage?> = _message

    fun dismissMessage() {
        _message.value = null
    }

    fun exportMaps() {
        withBusy {
            mapLibraryRepository.exportMaps().fold(
                onSuccess = { LibraryMessage.MapsReady(it) },
                onFailure = { LibraryMessage.Error("Не удалось выгрузить карты: ${it.message}") }
            )
        }
    }

    fun importMaps(uri: Uri) {
        withBusy {
            mapLibraryRepository.importMaps(uri).fold(
                onSuccess = { added ->
                    if (added > 0) {
                        LibraryMessage.Info("Добавлено карт: $added")
                    } else {
                        LibraryMessage.Info("Новых карт в файле не нашлось")
                    }
                },
                onFailure = { LibraryMessage.Error("Не удалось загрузить карты: ${it.message}") }
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
                        LibraryMessage.Info("Загружено точек: $count")
                    } else {
                        LibraryMessage.Info("В файле нет точек с координатами")
                    }
                },
                onFailure = { LibraryMessage.Error("Не удалось прочитать GPX: ${it.message}") }
            )
        }
    }

    /** Готовит GPX всех точек; экран отдаёт его системному «Поделиться». */
    suspend fun buildSpotsGpx(): String {
        val fishNames = fishRepository.getAllFish().first().associate { it.id to it.name }
        return GpxWriter.write(spots.value, fishNames)
    }

    private fun withBusy(block: suspend () -> LibraryMessage) {
        viewModelScope.launch {
            _busy.value = true
            _message.value = block()
            _busy.value = false
        }
    }
}
