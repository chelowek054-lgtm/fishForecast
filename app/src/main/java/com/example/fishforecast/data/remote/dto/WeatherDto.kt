package com.example.fishforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("hourly")
    val hourly: HourlyWeatherDataDto
)

@Serializable
data class HourlyWeatherDataDto(
    val time: List<String>,
    @SerialName("temperature_2m")
    val temperatures: List<Double>,
    @SerialName("relative_humidity_2m")
    val humidities: List<Double>,
    @SerialName("pressure_msl")
    val pressures: List<Double>,
    @SerialName("wind_speed_10m")
    val windSpeeds: List<Double>,
    @SerialName("wind_direction_10m")
    val windDirections: List<Double>,
    @SerialName("weather_code")
    val weatherCodes: List<Int>
)