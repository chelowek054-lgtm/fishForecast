package com.example.fishforecast.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.domain.location.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationTracker: LocationTracker
) : ViewModel() {

    /** Широта/долгота рыболова; null, пока позиция неизвестна. */
    private val _userLocation = mutableStateOf<Pair<Double, Double>?>(null)
    val userLocation: State<Pair<Double, Double>?> = _userLocation

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
}
