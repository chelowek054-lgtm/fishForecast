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
    /** Прогноз конкретной карты: у каждой свой, чтобы работать без сети. */
    fun forecastForMap(mapId: Int): Flow<List<WeatherEntity>> = dao.getForecastForMap(mapId)

    suspend fun fetchWeather(mapId: Int, lat: Double, lon: Double): Result<Unit> {
        return try {
            val response = api.getWeatherData(lat, lon)
            val entities = response.hourly.time.mapIndexed { index, time ->
                WeatherEntity(
                    mapId = mapId,
                    time = time,
                    temperature = response.hourly.temperatures[index],
                    humidity = response.hourly.humidities[index],
                    pressure = response.hourly.pressures[index],
                    windSpeed = response.hourly.windSpeeds[index],
                    windDirection = response.hourly.windDirections[index],
                    precipitationChance =
                        response.hourly.precipitationChances.getOrNull(index)?.toDouble() ?: 0.0,
                    weatherCode = response.hourly.weatherCodes[index],
                    latitude = lat,
                    longitude = lon
                )
            }
            dao.clearForecast(mapId)
            dao.insertForecast(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cleanOldData(mapId: Int) {
        // Open-Meteo отдаёт время без секунд, а сравнение в запросе строковое,
        // поэтому формат отсечки должен совпадать с форматом хранения.
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        dao.deleteOldForecast(mapId, now)
    }
}
