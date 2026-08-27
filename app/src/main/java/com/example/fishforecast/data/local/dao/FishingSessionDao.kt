package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fishforecast.data.local.entities.FishingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FishingSessionDao {

    /** Незавершённая рыбалка: она может быть только одна. */
    @Query("SELECT * FROM fishing_sessions WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActive(): Flow<FishingSessionEntity?>

    @Query("SELECT * FROM fishing_sessions WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun active(): FishingSessionEntity?

    @Query("SELECT * FROM fishing_sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<FishingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FishingSessionEntity): Long

    @Update
    suspend fun update(session: FishingSessionEntity)

    @Delete
    suspend fun delete(session: FishingSessionEntity)
}
