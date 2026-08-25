package com.example.fishforecast.ui.fishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FishListViewModel @Inject constructor(
    private val repository: FishRepository
) : ViewModel() {

    val fishList: StateFlow<List<FishEntity>> = repository.getAllFish()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.preloadDataIfNeeded()
        }
    }

    fun deleteFish(fish: FishEntity) {
        viewModelScope.launch {
            repository.deleteFish(fish)
        }
    }
}