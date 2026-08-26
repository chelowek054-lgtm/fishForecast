package com.example.fishforecast.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.CatchEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.repository.CatchRepository
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingSpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class JournalUiState(
    val catches: List<CatchEntity> = emptyList(),
    val fishList: List<FishEntity> = emptyList(),
    val spots: List<FishingSpotEntity> = emptyList()
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val catchRepository: CatchRepository,
    fishRepository: FishRepository,
    spotRepository: FishingSpotRepository
) : ViewModel() {

    val state: StateFlow<JournalUiState> = combine(
        catchRepository.catches,
        fishRepository.getAllFish(),
        spotRepository.spots
    ) { catches, fishList, spots ->
        JournalUiState(catches = catches, fishList = fishList, spots = spots)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalUiState()
    )

    /** Файл готовится заранее: камере нужен адрес до съёмки. */
    fun createPhotoFile(): File = catchRepository.createPhotoFile()

    fun addCatch(
        fish: FishEntity?,
        spot: FishingSpotEntity?,
        photoPath: String?,
        weightGrams: Int?,
        lengthCm: Int?,
        note: String
    ) {
        viewModelScope.launch {
            catchRepository.addCatch(
                entity = CatchEntity(
                    fishId = fish?.id,
                    spotId = spot?.id,
                    photoPath = photoPath,
                    weightGrams = weightGrams,
                    lengthCm = lengthCm,
                    note = note
                ),
                fish = fish
            )
        }
    }

    fun deleteCatch(entity: CatchEntity) {
        viewModelScope.launch {
            catchRepository.deleteCatch(entity)
        }
    }
}
