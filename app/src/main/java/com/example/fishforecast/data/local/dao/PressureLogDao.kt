package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.PressureLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PressureLogDao {

    @Query("SELECT * FROM barometer_log ORDER BY time ASC")
    fun getLog(): Flow<List<PressureLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PressureLogEntity)

    @Query("DELETE FROM barometer_log WHERE time < :oldest")
    suspend fun deleteOlderThan(oldest: String)
}
