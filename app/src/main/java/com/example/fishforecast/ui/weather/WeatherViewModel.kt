package com.example.fishforecast.ui.weather

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.local.entities.PressureLogEntity
import com.example.fishforecast.data.repository.BarometerRepository
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.domain.sensor.PressureProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.fishforecast.domain.water.WaterState
import com.example.fishforecast.domain.water.calculateWaterState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val fishingContext: FishingContextRepository,
    private val barometer: BarometerRepository,
    pressureProvider: PressureProvider
) : ViewModel() {

    init {
        // Датчик работает, только пока экран открыт, поэтому показания
        // складываются в базу сразу: своя история давления точнее сетевой и
        // не требует связи.
        if (pressureProvider.isAvailable) {
            viewModelScope.launch {
                pressureProvider.pressureFlow().collect { barometer.record(it) }
            }
        }
    }

    /** Показания барометра за прошедшие дни; ряд рваный — это нормально. */
    val pressureLog: StateFlow<List<PressureLogEntity>> = barometer.log
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Прогноз выбранного района, а не места, где сейчас телефон. */
    val forecast: StateFlow<List<WeatherEntity>> = fishingContext.activeForecast
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Восход и закат по дням: зори — главные окна клёва. */
    val sunTimes: StateFlow<List<DailySunEntity>> = fishingContext.activeSunTimes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeMap: StateFlow<SavedMapEntity?> = fishingContext.activeMap
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Норма давления района, мм рт. ст. От неё считается отклонение: общей
     * цифры не существует, рыба привыкает к фону своего водоёма.
     */
    val normalPressureMmHg: StateFlow<Double?> = fishingContext.activeMap
        .map { it?.normalPressureMmHg }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Ход воды в двух слоях. Считается по уже скачанному прогнозу вместе с
     * прошедшими сутками: без истории инерционная модель не разгоняется.
     */
    val water: StateFlow<WaterState> = combine(
        fishingContext.activeForecast,
        fishingContext.activeMap
    ) { forecast, map ->
        calculateWaterState(forecast, map)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = calculateWaterState(emptyList(), null)
    )

    val hasBarometer: Boolean = pressureProvider.isAvailable

    /** Показания местного барометра в гПа; null, пока датчик молчит. */
    val localPressure: StateFlow<Float?> = pressureProvider.pressureFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadWeatherInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            fishingContext.refreshWeather().onFailure {
                _error.value = "Не удалось загрузить прогноз: ${it.message ?: "нет сети"}"
            }

            _isLoading.value = false
        }
    }
}
