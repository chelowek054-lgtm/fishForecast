package com.example.fishforecast.ui.weather

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.domain.sensor.PressureProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val fishingContext: FishingContextRepository,
    pressureProvider: PressureProvider
) : ViewModel() {

    /** Прогноз выбранного района, а не места, где сейчас телефон. */
    val forecast: StateFlow<List<WeatherEntity>> = fishingContext.activeForecast
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
