package com.example.fishforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("hourly")
    val hourly: HourlyWeatherDataDto,
    @SerialName("daily")
    val daily: DailyWeatherDataDto? = null
)

@Serializable
data class HourlyWeatherDataDto(
    val time: List<String>,
    @SerialName("temperature_2m")
    val temperatures: List<Double>,
    @SerialName("relative_humidity_2m")
    val humidities: List<Double>,
    /** Станционное давление: его же показывает барометр устройства. */
    @SerialName("surface_pressure")
    val pressures: List<Double>,
    @SerialName("wind_speed_10m")
    val windSpeeds: List<Double>,
    @SerialName("wind_direction_10m")
    val windDirections: List<Double>,
    @SerialName("weather_code")
    val weatherCodes: List<Int>,
    /**
     * Некоторые модели отдают не все переменные для точки, поэтому списки
     * необязательные: отсутствие порывов не должно ронять весь прогноз.
     */
    @SerialName("wind_gusts_10m")
    val windGusts: List<Double?> = emptyList(),
    @SerialName("precipitation")
    val precipitation: List<Double?> = emptyList(),
    @SerialName("precipitation_probability")
    val precipitationChances: List<Int?> = emptyList(),
    @SerialName("cloud_cover")
    val cloudCover: List<Double?> = emptyList(),
    /** Приход солнца, Вт/м² — вход модели прогрева воды. */
    @SerialName("shortwave_radiation")
    val shortwaveRadiation: List<Double?> = emptyList()
)

@Serializable
data class DailyWeatherDataDto(
    val time: List<String>,
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList()
)
