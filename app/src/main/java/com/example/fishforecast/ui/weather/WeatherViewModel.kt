package com.example.fishforecast.ui.weather

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.repository.WeatherRepository
import com.example.fishforecast.domain.location.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    val forecast: StateFlow<List<WeatherEntity>> = repository.weatherForecast
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadWeatherInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                repository.fetchWeather(location.latitude, location.longitude)
                    .onFailure {
                        _error.value = "Не удалось загрузить прогноз: ${it.message ?: "нет сети"}"
                    }
            } else {
                _error.value = "Не удалось получить местоположение. Проверьте GPS и разрешения."
            }
            _isLoading.value = false
        }
    }
}