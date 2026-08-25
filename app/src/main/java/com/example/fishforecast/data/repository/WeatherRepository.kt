package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.WeatherDao
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.remote.WeatherApi
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val dao: WeatherDao
) {
    val weatherForecast: Flow<List<WeatherEntity>> = dao.getWeatherForecast()

    suspend fun fetchWeather(lat: Double, lon: Double) {
        try {
            val response = api.getWeatherData(lat, lon)
            val entities = response.hourly.time.mapIndexed { index, time ->
                WeatherEntity(
                    time = time,
                    temperature = response.hourly.temperatures[index],
                    humidity = response.hourly.humidities[index],
                    pressure = response.hourly.pressures[index],
                    windSpeed = response.hourly.windSpeeds[index],
                    weatherCode = response.hourly.weatherCodes[index],
                    latitude = lat,
                    longitude = lon
                )
            }
            dao.clearForecast()
            dao.insertForecast(entities)
        } catch (e: Exception) {
            // Handle error (e.g., log or emit to UI)
            e.printStackTrace()
        }
    }

    suspend fun cleanOldData() {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        dao.deleteOldForecast(now)
    }
}