package com.example.fishforecast.ui.bite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.domain.bite.BiteForecast
import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase
import com.example.fishforecast.domain.water.calculateWaterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class BiteUiState(
    val fishList: List<FishEntity> = emptyList(),
    val selectedFish: FishEntity? = null,
    val spots: List<FishingSpotEntity> = emptyList(),
    /** Точка уточняет норму давления карты; без неё берётся норма района. */
    val selectedSpot: FishingSpotEntity? = null,
    val activeMap: SavedMapEntity? = null,
    val forecast: List<BiteForecast> = emptyList(),
    /** Прогноза нет — считать нечего, и это не ошибка. */
    val weatherMissing: Boolean = false
)

@HiltViewModel
class BiteViewModel @Inject constructor(
    private val fishingContext: FishingContextRepository,
    fishRepository: FishRepository,
    calculateFishActivity: CalculateFishActivityUseCase
) : ViewModel() {

    private val selectedFishId = MutableStateFlow<Int?>(null)
    private val selectedSpotId = MutableStateFlow<Int?>(null)

    val state: StateFlow<BiteUiState> = combine(
        fishRepository.getAllFish(),
        fishingContext.activeForecast,
        fishingContext.activeSpots,
        fishingContext.activeMap,
        combine(selectedFishId, selectedSpotId) { fishId, spotId -> fishId to spotId }
    ) { fishList, weather, spots, map, (selectedId, spotId) ->
        // Пока рыболов не выбрал рыбу, показываем первую из справочника:
        // экран должен отвечать на вопрос «ехать или нет» сразу.
        val selected = fishList.firstOrNull { it.id == selectedId } ?: fishList.firstOrNull()

        // Считаем по всему прогнозу — трёхчасовой тенденции нужна история,
        // но показываем только то, что впереди: прошедшие часы решению
        // «ехать или нет» не помогают.
        val spot = spots.firstOrNull { it.id == spotId }
        val normalPressure = fishingContext.normalPressureFor(map)
        // Вода считается по тому же прогнозу вместе с прошедшими сутками:
        // без истории инерционная модель не разгоняется.
        val water = calculateWaterState(weather, map)
        val calculated = selected
            ?.let { calculateFishActivity(it, weather, normalPressure, water) }
            .orEmpty()
        // Час усекается: текущий час ещё идёт, и выбрасывать его нельзя.
        val fromNow = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).format(HOUR_FORMAT)

        BiteUiState(
            fishList = fishList,
            selectedFish = selected,
            spots = spots,
            selectedSpot = spot,
            activeMap = map,
            forecast = calculated.filter { it.time >= fromNow },
            weatherMissing = weather.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BiteUiState()
    )

    fun selectFish(fish: FishEntity) {
        selectedFishId.value = fish.id
    }

    fun selectSpot(spot: FishingSpotEntity?) {
        selectedSpotId.value = spot?.id
    }

    private companion object {
        /** Формат совпадает с ключом времени Open-Meteo, сравнение строковое. */
        val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }
}
