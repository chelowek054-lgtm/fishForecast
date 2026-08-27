package com.example.fishforecast.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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

    /** Схема или спутник — выбор рыболова, а не свойство района. */
    val baseLayer: Flow<String?> = context.settings.data.map { preferences ->
        preferences[BASE_LAYER]
    }

    suspend fun setBaseLayer(name: String) {
        context.settings.edit { preferences -> preferences[BASE_LAYER] = name }
    }

    /**
     * Кто этот рыболов для обмена пакетами. Идентификатор случайный и
     * заводится сам: требовать учётную запись ради отправки файла незачем,
     * а в общей базе нужно понимать, чей район. Имя — по желанию.
     */
    val authorId: Flow<String?> = context.settings.data.map { it[AUTHOR_ID] }

    val authorName: Flow<String?> = context.settings.data.map { it[AUTHOR_NAME] }

    /** Возвращает идентификатор, заводя его при первом обращении. */
    suspend fun ensureAuthorId(): String {
        var id = ""
        context.settings.edit { preferences ->
            id = preferences[AUTHOR_ID] ?: java.util.UUID.randomUUID().toString()
            preferences[AUTHOR_ID] = id
        }
        return id
    }

    suspend fun setAuthorName(name: String?) {
        context.settings.edit { preferences ->
            if (name.isNullOrBlank()) {
                preferences.remove(AUTHOR_NAME)
            } else {
                preferences[AUTHOR_NAME] = name
            }
        }
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
        val BASE_LAYER = stringPreferencesKey("base_layer")
        val AUTHOR_ID = stringPreferencesKey("author_id")
        val AUTHOR_NAME = stringPreferencesKey("author_name")
    }
}
