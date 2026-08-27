package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_forecast WHERE mapId = :mapId ORDER BY time ASC")
    fun getForecastForMap(mapId: Int): Flow<List<WeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: List<WeatherEntity>)

    @Query("DELETE FROM weather_forecast WHERE mapId = :mapId AND time < :currentTime")
    suspend fun deleteOldForecast(mapId: Int, currentTime: String)

    @Query("DELETE FROM weather_forecast WHERE mapId = :mapId")
    suspend fun clearForecast(mapId: Int)

    @Query("SELECT * FROM daily_sun WHERE mapId = :mapId ORDER BY date ASC")
    fun getSunTimesForMap(mapId: Int): Flow<List<DailySunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSunTimes(days: List<DailySunEntity>)

    @Query("DELETE FROM daily_sun WHERE mapId = :mapId AND date < :currentDate")
    suspend fun deleteOldSunTimes(mapId: Int, currentDate: String)
}
