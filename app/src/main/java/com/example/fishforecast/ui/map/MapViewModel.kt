package com.example.fishforecast.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.data.repository.FishingSpotRepository
import com.example.fishforecast.data.repository.OfflineMapRepository
import com.example.fishforecast.data.repository.RegionDownloadState
import com.example.fishforecast.domain.location.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLngBounds
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val offlineMapRepository: OfflineMapRepository,
    private val fishingSpotRepository: FishingSpotRepository,
    private val fishingContext: FishingContextRepository,
    fishRepository: FishRepository
) : ViewModel() {

    /** Широта/долгота рыболова; null, пока позиция неизвестна. */
    private val _userLocation = mutableStateOf<Pair<Double, Double>?>(null)
    val userLocation: State<Pair<Double, Double>?> = _userLocation

    val baseLayer: StateFlow<BaseLayer> = fishingContext.baseLayer
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseLayer.SCHEME
        )

    val activeMap: StateFlow<SavedMapEntity?> = fishingContext.activeMap
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val spots: StateFlow<List<FishingSpotEntity>> = fishingSpotRepository.spots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Справочник нужен, чтобы привязать точку к рыбе, которая здесь берёт. */
    val fishList: StateFlow<List<FishEntity>> = fishRepository.getAllFish()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _downloadState = mutableStateOf<RegionDownloadState?>(null)
    val downloadState: State<RegionDownloadState?> = _downloadState

    init {
        locateUser()
    }

    fun locateUser() {
        viewModelScope.launch {
            locationTracker.getCurrentLocation()?.let { location ->
                _userLocation.value = location.latitude to location.longitude
            }
        }
    }

    /**
     * Сохраняет то, что сейчас на экране. Нижняя граница масштаба берётся
     * от текущего вида, верхняя — из конфига: тайлы крупнее нужны на воде,
     * но именно они дают основной объём.
     */
    fun saveVisibleRegion(
        name: String,
        bounds: LatLngBounds,
        currentZoom: Double,
        normalPressureMmHg: Double? = null
    ) {
        viewModelScope.launch {
            val zoomRange = MapConfig.offlineZoomRange(currentZoom)
            offlineMapRepository.downloadRegion(
                name = name,
                bounds = bounds,
                minZoom = zoomRange.start,
                maxZoom = zoomRange.endInclusive,
                normalPressureMmHg = normalPressureMmHg
            ).collect { state ->
                _downloadState.value = state
                // Только что сохранённый район сразу становится рабочим:
                // ради него рыболов и нажимал кнопку.
                if (state is RegionDownloadState.Done) {
                    fishingContext.activateLatestMap()
                }
            }
        }
    }

    fun selectBaseLayer(layer: BaseLayer) {
        viewModelScope.launch { fishingContext.setBaseLayer(layer) }
    }

    fun dismissDownloadState() {
        _downloadState.value = null
    }

    fun addSpot(
        name: String,
        latitude: Double,
        longitude: Double,
        fishId: Int?,
        note: String,
        normalPressureMmHg: Double? = null
    ) {
        viewModelScope.launch {
            fishingSpotRepository.addSpot(
                FishingSpotEntity(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    fishId = fishId,
                    note = note,
                    normalPressureMmHg = normalPressureMmHg
                )
            )
        }
    }

    fun deleteSpot(spot: FishingSpotEntity) {
        viewModelScope.launch {
            fishingSpotRepository.deleteSpot(spot)
        }
    }

    fun deleteRegion(region: SavedMapEntity) {
        viewModelScope.launch {
            offlineMapRepository.deleteRegion(region)
        }
    }
}
