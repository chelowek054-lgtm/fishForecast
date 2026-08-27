package com.example.fishforecast.ui.reference

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.domain.light.LightPhase
import com.example.fishforecast.domain.light.lightPhaseAt
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.KnowledgeRepository
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase
import com.example.fishforecast.domain.water.WaterState
import com.example.fishforecast.domain.water.oxygenSaturationMgL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Вид справочника вместе с тем, что он значит прямо сейчас.
 *
 * Список рыб сам по себе — таблица цифр. Рыболову нужно другое: кто сегодня
 * на этом водоёме кормится, а кто стоит, — поэтому каждый вид приходит на
 * экран уже сопоставленным с водой и кислородом активного района.
 */
data class FishCard(
    val fish: FishEntity,
    /** Оценка клёва на ближайший час; null — прогноза ещё нет. */
    val score: Int?,
    /** Температура мели, °C; null — район не выбран. */
    val waterTemperature: Double?,
    val oxygenMgL: Double?,
    /** Фаза света этого часа; null — данных о восходе ещё нет. */
    val lightPhase: LightPhase? = null
) {
    /** Вода холоднее порога — у вида холодный стол. */
    val coldTable: Boolean
        get() = (waterTemperature ?: Double.MAX_VALUE) < fish.coldTempThreshold
}

@HiltViewModel
class ReferenceViewModel @Inject constructor(
    private val repository: FishRepository,
    private val knowledge: KnowledgeRepository,
    private val fishingContext: FishingContextRepository,
    private val calculateFishActivity: CalculateFishActivityUseCase
) : ViewModel() {

    val cards: StateFlow<List<FishCard>> = combine(
        repository.getAllFish(),
        fishingContext.activeForecast,
        fishingContext.activeMap,
        fishingContext.activeWater,
        fishingContext.activeSunTimes
    ) { fishList, forecast, map, water, sunTimes ->
        val normal = fishingContext.normalPressureFor(map)

        fishList
            .map { fish -> fish.toCard(forecast, water, normal, sunTimes) }
            // Кто сегодня активнее — тот и выше: справочник должен отвечать
            // на вопрос «за кем ехать», а не хранить алфавитный порядок.
            .sortedWith(compareByDescending<FishCard> { it.score ?: -1 }.thenBy { it.fish.name })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Словари знаний: типы водоёмов, структуры, наблюдения. */
    val knowledgeCatalog: StateFlow<KnowledgeCatalog> = knowledge.catalog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KnowledgeCatalog())

    val knowledgeUrl: StateFlow<String?> = knowledge.knowledgeUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val knowledgeVersion: StateFlow<Int> = knowledge.knowledgeVersion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val catalogUrl: StateFlow<String?> = repository.catalogUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val catalogVersion: StateFlow<Int> = repository.catalogVersion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _busy = mutableStateOf(false)
    val busy: State<Boolean> = _busy

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.syncBuiltInCatalog().onFailure {
                _error.value = "Не удалось загрузить справочник: ${it.message}"
            }
        }
    }

    private fun FishEntity.toCard(
        forecast: List<com.example.fishforecast.data.local.entities.WeatherEntity>,
        water: WaterState,
        normalPressureMmHg: Double?,
        sunTimes: List<DailySunEntity>
    ): FishCard {
        if (forecast.isEmpty()) return FishCard(this, null, null, null)

        val now = LocalDateTime.now()
        val hour = forecast.minByOrNull {
            kotlin.math.abs(
                java.time.Duration.between(LocalDateTime.parse(it.time), now).toMinutes()
            )
        }
        val score = hour?.let {
            calculateFishActivity(this, forecast, normalPressureMmHg, water, sunTimes)
                .firstOrNull { forecastHour -> forecastHour.time == it.time }
                ?.score
        }
        val waterNow = hour?.let { water.shallowAt(it.time) }
        val phase = hour?.let {
            val moment = LocalDateTime.parse(it.time)
            lightPhaseAt(moment, sunTimes.firstOrNull { day -> day.date == moment.toLocalDate().toString() })
        }

        return FishCard(
            fish = this,
            score = score,
            waterTemperature = waterNow,
            // Кислород берётся у воды: он зависит от типа водоёма и ночи,
            // а не только от температуры.
            oxygenMgL = hour?.let { water.oxygenAt(it.time) }
                ?: waterNow?.let { oxygenSaturationMgL(it) },
            lightPhase = phase
        )
    }

    /** Обновляет справочник с сервера, если задан адрес. */
    fun refreshCatalog() {
        viewModelScope.launch {
            _busy.value = true
            repository.refreshCatalogFromServer().fold(
                onSuccess = { update ->
                    _messages.send(
                        when {
                            update.upToDate -> "Справочник актуален (версия ${update.version})"
                            update.changed == 0 -> "Изменений нет"
                            else -> "Обновлено видов: ${update.changed}"
                        }
                    )
                },
                onFailure = { _messages.send("Не удалось обновить: ${it.message}") }
            )
            _busy.value = false
        }
    }

    /** Обновляет словари знаний с сервера, если задан адрес. */
    fun refreshKnowledge() {
        viewModelScope.launch {
            _busy.value = true
            knowledge.refreshFromServer().fold(
                onSuccess = { update ->
                    _messages.send(
                        if (update.upToDate) {
                            "Словари актуальны (версия ${update.version})"
                        } else {
                            "Словари обновлены до версии ${update.version}"
                        }
                    )
                },
                onFailure = { _messages.send("Не удалось обновить словари: ${it.message}") }
            )
            _busy.value = false
        }
    }

    fun setKnowledgeUrl(url: String?) {
        viewModelScope.launch { knowledge.setKnowledgeUrl(url) }
    }

    fun restoreBuiltInKnowledge() {
        viewModelScope.launch {
            knowledge.restoreBuiltIn()
            _messages.send("Встроенные словари восстановлены")
        }
    }

    fun setCatalogUrl(url: String?) {
        viewModelScope.launch {
            repository.setCatalogUrl(url)
            _messages.send(if (url.isNullOrBlank()) "Источник сброшен" else "Источник сохранён")
        }
    }

    /** Возвращает справочник к встроенному — на случай неудачного обновления. */
    fun restoreBuiltInCatalog() {
        viewModelScope.launch {
            _busy.value = true
            repository.restoreCatalogFromAssets().fold(
                onSuccess = { _messages.send("Встроенный справочник восстановлен") },
                onFailure = { _messages.send("Не удалось восстановить: ${it.message}") }
            )
            _busy.value = false
        }
    }

    fun deleteFish(fish: FishEntity) {
        viewModelScope.launch { repository.deleteFish(fish) }
    }
}
