package com.example.fishforecast.data.local.dao

import androidx.room.*
import com.example.fishforecast.data.local.entities.FishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {
    @Query("SELECT * FROM fish")
    fun getAllFish(): Flow<List<FishEntity>>

    @Query("SELECT * FROM fish WHERE id = :id")
    suspend fun getFishById(id: Int): FishEntity?

    @Query("SELECT * FROM fish")
    suspend fun allFish(): List<FishEntity>

    /** Поиск по глобальному идентификатору: так узнаётся чужой вид. */
    @Query("SELECT * FROM fish WHERE uid = :uid")
    suspend fun getFishByUid(uid: String): FishEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: FishEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fishList: List<FishEntity>)

    @Delete
    suspend fun deleteFish(fish: FishEntity)
}