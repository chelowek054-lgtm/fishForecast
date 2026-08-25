package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_forecast ORDER BY time ASC")
    fun getWeatherForecast(): Flow<List<WeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: List<WeatherEntity>)

    @Query("DELETE FROM weather_forecast WHERE time < :currentTime")
    suspend fun deleteOldForecast(currentTime: String)

    @Query("DELETE FROM weather_forecast")
    suspend fun clearForecast()
}