package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
