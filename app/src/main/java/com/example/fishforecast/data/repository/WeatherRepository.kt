package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.WeatherDao
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.remote.WeatherApi
import com.example.fishforecast.domain.bite.averagePressureMmHg
import com.example.fishforecast.domain.bite.standardPressureMmHg
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Оценка нормы давления: сама цифра и то, откуда она взялась. Рыболову
 * важно отличать среднее по наблюдениям от прикидки по высоте.
 */
data class NormalPressureEstimate(
    val normalMmHg: Double,
    val elevationM: Double?,
    val fromHistory: Boolean
)

@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val dao: WeatherDao
) {
    /** Прогноз конкретной карты: у каждой свой, чтобы работать без сети. */
    fun forecastForMap(mapId: Int): Flow<List<WeatherEntity>> = dao.getForecastForMap(mapId)

    fun sunTimesForMap(mapId: Int): Flow<List<DailySunEntity>> = dao.getSunTimesForMap(mapId)

    /**
     * Норма давления места по истории наблюдений.
     *
     * Возвращает и высоту: если ряд оказался короче двух недель, среднее
     * описывает погоду недели, а не норму, — тогда цифру даёт стандартная
     * атмосфера на этой высоте.
     */
    suspend fun fetchNormalPressure(lat: Double, lon: Double): Result<NormalPressureEstimate> {
        return try {
            val response = api.getPressureHistory(lat, lon)
            val measured = averagePressureMmHg(response.hourly.pressures)
            val elevation = response.elevation
            val value = measured
                ?: elevation?.let { standardPressureMmHg(it) }
                ?: return Result.failure(IllegalStateException("Нет ни истории, ни высоты"))

            Result.success(
                NormalPressureEstimate(
                    normalMmHg = value,
                    elevationM = elevation,
                    fromHistory = measured != null
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchWeather(mapId: Int, lat: Double, lon: Double): Result<Double?> {
        return try {
            val response = api.getWeatherData(lat, lon)
            val hourly = response.hourly
            val entities = hourly.time.mapIndexed { index, time ->
                WeatherEntity(
                    mapId = mapId,
                    time = time,
                    temperature = hourly.temperatures[index],
                    humidity = hourly.humidities[index],
                    pressure = hourly.pressures[index],
                    windSpeed = hourly.windSpeeds[index],
                    windDirection = hourly.windDirections[index],
                    windGusts = hourly.windGusts.getOrNull(index) ?: 0.0,
                    precipitationChance =
                        hourly.precipitationChances.getOrNull(index)?.toDouble() ?: 0.0,
                    precipitation = hourly.precipitation.getOrNull(index) ?: 0.0,
                    cloudCover = hourly.cloudCover.getOrNull(index) ?: 0.0,
                    shortwaveRadiation = hourly.shortwaveRadiation.getOrNull(index) ?: 0.0,
                    weatherCode = hourly.weatherCodes[index],
                    latitude = lat,
                    longitude = lon
                )
            }
            dao.clearForecast(mapId)
            dao.insertForecast(entities)

            response.daily?.let { daily ->
                dao.insertSunTimes(
                    daily.time.mapIndexedNotNull { index, date ->
                        val sunrise = daily.sunrise.getOrNull(index) ?: return@mapIndexedNotNull null
                        val sunset = daily.sunset.getOrNull(index) ?: return@mapIndexedNotNull null
                        DailySunEntity(mapId, date, sunrise, sunset)
                    }
                )
            }
            Result.success(response.elevation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Чистка кэша. Прошедшие сутки остаются намеренно: без них не видно
     * похолодания после жары, а модель воды теряет разгон. Уходит только то,
     * что старше окна истории.
     */
    suspend fun cleanOldData(mapId: Int) {
        // Open-Meteo отдаёт время без секунд, а сравнение в запросе строковое,
        // поэтому формат отсечки должен совпадать с форматом хранения.
        val cutoff = LocalDateTime.now().minusDays(WeatherApi.PAST_DAYS.toLong())
        dao.deleteOldForecast(
            mapId = mapId,
            currentTime = cutoff.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        )
        dao.deleteOldSunTimes(
            mapId = mapId,
            currentDate = LocalDate.now().minusDays(WeatherApi.PAST_DAYS.toLong()).toString()
        )
    }
}
