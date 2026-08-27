package com.example.fishforecast.data.local.entities

import androidx.room.Entity

/**
 * Час прогноза для конкретной сохранённой карты.
 *
 * Ключ составной: у каждой карты свой прогноз, иначе переключение района
 * затирало бы данные предыдущего — а без сети вернуть их будет неоткуда.
 */
@Entity(tableName = "weather_forecast", primaryKeys = ["mapId", "time"])
data class WeatherEntity(
    val mapId: Int,
    /** ISO8601 без секунд, как отдаёт Open-Meteo. */
    val time: String,
    val temperature: Double,
    val humidity: Double,
    val pressure: Double,
    val windSpeed: Double,
    /** Откуда дует, градусы. Нужен для оценки прогрева и перемешивания воды. */
    val windDirection: Double = 0.0,
    /** Вероятность осадков, %. Решает, брать ли с собой дождевик. */
    val precipitationChance: Double = 0.0,
    val weatherCode: Int,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)
