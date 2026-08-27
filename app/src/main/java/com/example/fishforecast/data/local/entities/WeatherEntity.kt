package com.example.fishforecast.data.local.entities

import androidx.room.Entity

/**
 * Час прогноза для конкретной сохранённой карты.
 *
 * Ключ составной: у каждой карты свой прогноз, иначе переключение района
 * затирало бы данные предыдущего — а без сети вернуть их будет неоткуда.
 *
 * Прошедшие часы тоже лежат здесь: похолодание после жары видно только в
 * сравнении с тем, что было, а инерционная модель воды без истории не
 * разгоняется.
 */
@Entity(tableName = "weather_forecast", primaryKeys = ["mapId", "time"])
data class WeatherEntity(
    val mapId: Int,
    /** ISO8601 без секунд, местное время района: запрос идёт с timezone=auto. */
    val time: String,
    val temperature: Double,
    val humidity: Double,
    /**
     * Станционное давление, гПа — то же, что показывает барометр устройства
     * и аневроид на стене. Приведённое к уровню моря сюда не попадает: под
     * Москвой оно на 14 мм рт. ст. выше, и норма водоёма с ним не сходится.
     */
    val pressure: Double,
    val windSpeed: Double,
    /** Откуда дует, градусы. Нужен для оценки прогрева и перемешивания воды. */
    val windDirection: Double = 0.0,
    /** Порывы, км/ч: по ним понятно, возможна ли ловля вообще. */
    val windGusts: Double = 0.0,
    /** Вероятность осадков, %. Решает, брать ли с собой дождевик. */
    val precipitationChance: Double = 0.0,
    /** Сколько выпадет, мм: ливень меняет воду сильнее, чем морось. */
    val precipitation: Double = 0.0,
    /** Облачность, %. Через неё солнце греет воду или не греет. */
    val cloudCover: Double = 0.0,
    /** Приход солнечной радиации, Вт/м² — вход модели температуры воды. */
    val shortwaveRadiation: Double = 0.0,
    val weatherCode: Int,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)
