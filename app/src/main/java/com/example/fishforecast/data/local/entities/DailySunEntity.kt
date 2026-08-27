package com.example.fishforecast.data.local.entities

import androidx.room.Entity

/**
 * Восход и закат по дням для сохранённой карты.
 *
 * Зори — главные окна клёва, и час рассвета сдвигается быстрее, чем кажется:
 * за месяц набегает больше часа. Хранится отдельно от почасового прогноза,
 * потому что это свойство дня, а не часа.
 */
@Entity(tableName = "daily_sun", primaryKeys = ["mapId", "date"])
data class DailySunEntity(
    val mapId: Int,
    /** Дата в ISO8601, местная. */
    val date: String,
    /** ISO8601 без секунд, местное время района. */
    val sunrise: String,
    val sunset: String
)
