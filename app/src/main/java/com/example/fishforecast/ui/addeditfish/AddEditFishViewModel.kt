package com.example.fishforecast.ui.addeditfish

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.ui.navigation.AddEditFishRoute
import com.example.fishforecast.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditFishViewModel @Inject constructor(
    private val repository: FishRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _fishName = mutableStateOf("")
    val fishName: State<String> = _fishName

    private val _fishDescription = mutableStateOf("")
    val fishDescription: State<String> = _fishDescription

    private val _minTemp = mutableStateOf("")
    val minTemp: State<String> = _minTemp

    private val _maxTemp = mutableStateOf("")
    val maxTemp: State<String> = _maxTemp

    private val _minPressure = mutableStateOf("")
    val minPressure: State<String> = _minPressure

    private val _maxPressure = mutableStateOf("")
    val maxPressure: State<String> = _maxPressure

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentFishId: Int? = null

    init {
        val fishId = savedStateHandle.toRoute<AddEditFishRoute>().fishId
        if (fishId != AddEditFishRoute.NEW_FISH_ID) {
            viewModelScope.launch {
                repository.getFishById(fishId)?.also { fish ->
                    currentFishId = fish.id
                    _fishName.value = fish.name
                    _fishDescription.value = fish.description
                    _minTemp.value = fish.minTemp.toString()
                    _maxTemp.value = fish.maxTemp.toString()
                    _minPressure.value = fish.minPressure.toString()
                    _maxPressure.value = fish.maxPressure.toString()
                }
            }
        }
    }

    fun onEvent(event: AddEditFishEvent) {
        when (event) {
            is AddEditFishEvent.EnteredName -> _fishName.value = event.value
            is AddEditFishEvent.EnteredDescription -> _fishDescription.value = event.value
            is AddEditFishEvent.EnteredMinTemp -> _minTemp.value = event.value
            is AddEditFishEvent.EnteredMaxTemp -> _maxTemp.value = event.value
            is AddEditFishEvent.EnteredMinPressure -> _minPressure.value = event.value
            is AddEditFishEvent.EnteredMaxPressure -> _maxPressure.value = event.value
            is AddEditFishEvent.SaveFish -> {
                viewModelScope.launch {
                    try {
                        repository.insertFish(
                            FishEntity(
                                id = currentFishId ?: 0,
                                name = fishName.value,
                                description = fishDescription.value,
                                minTemp = minTemp.value.toFloatOrNull() ?: 0f,
                                maxTemp = maxTemp.value.toFloatOrNull() ?: 0f,
                                minPressure = minPressure.value.toFloatOrNull() ?: 0f,
                                maxPressure = maxPressure.value.toFloatOrNull() ?: 0f
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveFish)
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Ошибка сохранения"))
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveFish : UiEvent()
    }
}