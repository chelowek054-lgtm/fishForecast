package com.example.fishforecast.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.repository.FishingContextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Выбор района для шапки.
 *
 * Отдельная модель, а не поле в каждой существующей: район — общий контекст
 * приложения, и держать его копию в пяти экранах значило бы пять раз описать
 * одно и то же.
 *
 * Переключение меняет одну настройку. Всё остальное — прогноз, вода, зори,
 * точки — уже подписано на неё через [FishingContextRepository] и подтянется
 * само из того, что сохранено для этой карты.
 */
@HiltViewModel
class ActiveMapViewModel @Inject constructor(
    private val fishingContext: FishingContextRepository
) : ViewModel() {

    val savedMaps: StateFlow<List<SavedMapEntity>> = fishingContext.savedMaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), emptyList())

    val activeMap: StateFlow<SavedMapEntity?> = fishingContext.activeMap
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    fun select(mapId: Int) {
        viewModelScope.launch { fishingContext.setActiveMap(mapId) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5000L
    }
}
