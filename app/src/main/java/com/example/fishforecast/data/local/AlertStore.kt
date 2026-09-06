package com.example.fishforecast.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fishforecast.domain.alert.AlertHistory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Память уведомлений: когда звали в прошлый раз и о каком окне.
 *
 * Без неё одно и то же окно объявлялось при каждой проверке — четырежды за
 * сутки об одном вечернем клёве. Это настройка поведения, а не данные о рыбе,
 * поэтому DataStore, а не Room.
 */
@Singleton
class AlertStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val history: Flow<AlertHistory> = context.settings.data.map { preferences ->
        AlertHistory(
            lastNotifiedAt = preferences[LAST_NOTIFIED_AT]?.toLocalDateTimeOrNull(),
            lastWindowTime = preferences[LAST_WINDOW_TIME]
        )
    }

    suspend fun remember(notifiedAt: LocalDateTime, windowTime: String) {
        context.settings.edit { preferences ->
            preferences[LAST_NOTIFIED_AT] = notifiedAt.toString()
            preferences[LAST_WINDOW_TIME] = windowTime
        }
    }

    /** Чужая или испорченная запись не должна ронять фоновую проверку. */
    private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
        runCatching { LocalDateTime.parse(this) }.getOrNull()

    private companion object {
        val LAST_NOTIFIED_AT = stringPreferencesKey("alert_last_notified_at")
        val LAST_WINDOW_TIME = stringPreferencesKey("alert_last_window_time")
    }
}
