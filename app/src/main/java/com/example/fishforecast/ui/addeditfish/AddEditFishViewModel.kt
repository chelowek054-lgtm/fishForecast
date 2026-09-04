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

    private val _maxPressureRiseDrop = mutableStateOf("")
    val maxPressureDrop: State<String> = _maxPressureRiseDrop

    private val _maxPressureRise = mutableStateOf("")
    val maxPressureRise: State<String> = _maxPressureRise

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
                    _maxPressureRiseDrop.value = fish.maxPressureDrop.toString()
                    _maxPressureRise.value = fish.maxPressureRise.toString()
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
            is AddEditFishEvent.EnteredPressureDrop -> _maxPressureRiseDrop.value = event.value
            is AddEditFishEvent.EnteredPressureRise -> _maxPressureRise.value = event.value
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
                                maxPressureDrop = DEFAULT_PRESSURE_TOLERANCE,
                                maxPressureRise = DEFAULT_PRESSURE_TOLERANCE
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
                                // Пустое поле значит «не выяснено», а не «ноль»:
                                // нулевой допуск выключил бы клёв у любого
                                // отклонения давления.
                                maxPressureDrop = maxPressureDrop.value.toFloatOrNull()
                                    ?: DEFAULT_PRESSURE_TOLERANCE,
                                maxPressureRise = maxPressureRise.value.toFloatOrNull()
                                    ?: DEFAULT_PRESSURE_TOLERANCE
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

    private companion object {
        /** Столько терпит вид, про который ничего не сказано, мм рт. ст. */
        const val DEFAULT_PRESSURE_TOLERANCE = 12f
    }
}
