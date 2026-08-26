package com.example.fishforecast.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settings by preferencesDataStore(name = "fish_forecast_settings")

/**
 * Какая карта сейчас выбрана. Это настройка, а не данные, поэтому живёт в
 * DataStore: Room остаётся источником истины для самих карт.
 */
@Singleton
class ActiveMapStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val activeMapId: Flow<Int?> = context.settings.data.map { preferences ->
        preferences[ACTIVE_MAP_ID]
    }

    suspend fun setActiveMapId(id: Int?) {
        context.settings.edit { preferences ->
            if (id == null) {
                preferences.remove(ACTIVE_MAP_ID)
            } else {
                preferences[ACTIVE_MAP_ID] = id
            }
        }
    }

    private companion object {
        val ACTIVE_MAP_ID = intPreferencesKey("active_map_id")
    }
}
