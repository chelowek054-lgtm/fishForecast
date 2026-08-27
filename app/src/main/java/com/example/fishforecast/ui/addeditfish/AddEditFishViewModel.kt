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

    /** Оптимум: в этих градусах рыба кормится охотно. */
    private val _optMinTemp = mutableStateOf("")
    val optMinTemp: State<String> = _optMinTemp

    private val _optMaxTemp = mutableStateOf("")
    val optMaxTemp: State<String> = _optMaxTemp

    /** Предел выносливости: за ним активность уходит в ноль. */
    private val _absMinTemp = mutableStateOf("")
    val absMinTemp: State<String> = _absMinTemp

    private val _absMaxTemp = mutableStateOf("")
    val absMaxTemp: State<String> = _absMaxTemp

    private val _minPressure = mutableStateOf("")
    val minPressure: State<String> = _minPressure

    private val _maxPressure = mutableStateOf("")
    val maxPressure: State<String> = _maxPressure

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    /**
     * Редактируемая запись целиком. Экран правит только часть полей, а
     * наживки, прикормка и пороги кислорода приходят из справочника —
     * пересобирать вид с нуля значило бы их потерять.
     */
    private var editedFish: FishEntity? = null

    init {
        val fishId = savedStateHandle.toRoute<AddEditFishRoute>().fishId
        if (fishId != AddEditFishRoute.NEW_FISH_ID) {
            viewModelScope.launch {
                repository.getFishById(fishId)?.also { fish ->
                    editedFish = fish
                    _fishName.value = fish.name
                    _fishDescription.value = fish.description
                    _optMinTemp.value = fish.optMinTemp.toString()
                    _optMaxTemp.value = fish.optMaxTemp.toString()
                    _absMinTemp.value = fish.absMinTemp.toString()
                    _absMaxTemp.value = fish.absMaxTemp.toString()
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
            is AddEditFishEvent.EnteredOptMinTemp -> _optMinTemp.value = event.value
            is AddEditFishEvent.EnteredOptMaxTemp -> _optMaxTemp.value = event.value
            is AddEditFishEvent.EnteredAbsMinTemp -> _absMinTemp.value = event.value
            is AddEditFishEvent.EnteredAbsMaxTemp -> _absMaxTemp.value = event.value
            is AddEditFishEvent.EnteredMinPressure -> _minPressure.value = event.value
            is AddEditFishEvent.EnteredMaxPressure -> _maxPressure.value = event.value
            is AddEditFishEvent.SaveFish -> {
                viewModelScope.launch {
                    try {
                        val optMin = optMinTemp.value.toFloatOrNull() ?: 0f
                        val optMax = optMaxTemp.value.toFloatOrNull() ?: 0f
                        repository.insertFish(
                            (editedFish ?: FishEntity(
                                name = "",
                                optMinTemp = optMin,
                                optMaxTemp = optMax,
                                absMinTemp = optMin,
                                absMaxTemp = optMax,
                                minPressure = 0f,
                                maxPressure = 0f
                            )).copy(
                                name = fishName.value,
                                description = fishDescription.value,
                                optMinTemp = optMin,
                                optMaxTemp = optMax,
                                // Предел не бывает уже оптимума: пустое поле
                                // значит «не выяснено», а не «ноль».
                                absMinTemp = absMinTemp.value.toFloatOrNull()?.coerceAtMost(optMin)
                                    ?: optMin,
                                absMaxTemp = absMaxTemp.value.toFloatOrNull()?.coerceAtLeast(optMax)
                                    ?: optMax,
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