package com.example.fishforecast.ui.bite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.domain.bite.BiteForecast
import com.example.fishforecast.data.local.entities.ObservationEntity
import com.example.fishforecast.data.repository.KnowledgeRepository
import com.example.fishforecast.data.repository.ObservationRepository
import com.example.fishforecast.data.repository.withTypes
import com.example.fishforecast.domain.knowledge.ObservationType
import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase
import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.bite.placeOf
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.domain.water.WaterState
import com.example.fishforecast.domain.weather.hourWindow
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
    /** Где в ряду текущий час: по нему рисуется разделитель «сейчас». */
    val nowIndex: Int = -1,
    /** Прогноза нет — считать нечего, и это не ошибка. */
    val weatherMissing: Boolean = false,
    /** Что вообще можно отметить: словарь наблюдений. */
    val observationTypes: List<ObservationType> = emptyList(),
    /** Что уже отмечено на этом районе. */
    val noted: List<ObservationEntity> = emptyList()
)

/** Всё, что описывает район: карта, вода, солнце и словари знаний. */
private data class BiteContext(
    val map: SavedMapEntity?,
    val water: WaterState,
    val sunTimes: List<DailySunEntity>,
    val knowledge: KnowledgeCatalog
)

@HiltViewModel
class BiteViewModel @Inject constructor(
    private val fishingContext: FishingContextRepository,
    fishRepository: FishRepository,
    knowledge: KnowledgeRepository,
    private val observations: ObservationRepository,
    calculateFishActivity: CalculateFishActivityUseCase
) : ViewModel() {

    private val selectedFishId = MutableStateFlow<Int?>(null)
    private val selectedSpotId = MutableStateFlow<Int?>(null)

    val state: StateFlow<BiteUiState> = combine(
        fishRepository.getAllFish(),
        fishingContext.activeForecast,
        fishingContext.activeSpots,
        combine(
            fishingContext.activeMap,
            fishingContext.activeWater,
            fishingContext.activeSunTimes,
            knowledge.catalog
        ) { map, water, sun, catalog -> BiteContext(map, water, sun, catalog) },
        combine(
            selectedFishId,
            selectedSpotId,
            observations.active
        ) { fishId, spotId, noted -> Triple(fishId, spotId, noted) }
    ) { fishList, weather, spots, context, (selectedId, spotId, noted) ->
        val (map, water, sunTimes, catalog) = context
        // Пока рыболов не выбрал рыбу, показываем первую из справочника:
        // экран должен отвечать на вопрос «ехать или нет» сразу.
        val selected = fishList.firstOrNull { it.id == selectedId } ?: fishList.firstOrNull()

        // Считаем по всему прогнозу — трёхчасовой тенденции нужна история,
        // но показываем только то, что впереди: прошедшие часы решению
        // «ехать или нет» не помогают.
        val spot = spots.firstOrNull { it.id == spotId }
        val normalPressure = fishingContext.normalPressureFor(map)
        // Структуры выбранной точки идут в расчёт: коряжник, бровка и приток
        // меняют шанс сильнее, чем разница в пару миллиметров давления.
        val place = placeOf(spot, WaterLayerChoice.SHALLOW, catalog)
        // Отметки рыболова: факт весомее прогноза, но со своим сроком жизни.
        val noticed = noted.withTypes(catalog)
        val calculated = selected
            ?.let {
                calculateFishActivity(
                    it, weather, normalPressure, water, sunTimes, place, noticed
                )
            }
            .orEmpty()
        // Прошедшие часы больше не выбрасываются: клёв читается в ходе, а
        // не в моментальном срезе. Отметку «сейчас» ставит окно.
        val window = hourWindow(calculated, HOURS_BACK, HOURS_FORWARD) { it.time }

        BiteUiState(
            fishList = fishList,
            selectedFish = selected,
            spots = spots,
            selectedSpot = spot,
            activeMap = map,
            forecast = window.items,
            nowIndex = window.nowIndex,
            weatherMissing = weather.isEmpty(),
            observationTypes = catalog.observations,
            noted = noted
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

    /** Отметить увиденное. Срок жизни отметки берётся из словаря знаний. */
    fun note(typeId: String) {
        viewModelScope.launch { observations.note(typeId) }
    }

    fun removeNote(observation: ObservationEntity) {
        viewModelScope.launch { observations.remove(observation) }
    }

    private companion object {
        /** Формат совпадает с ключом времени Open-Meteo, сравнение строковое. */
        val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }
}
