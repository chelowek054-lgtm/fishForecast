package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.MapRegionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapRegionDao {

    @Query("SELECT * FROM map_regions ORDER BY createdAt DESC")
    fun getRegions(): Flow<List<MapRegionEntity>>

    @Query("SELECT * FROM map_regions WHERE id = :id")
    suspend fun getRegionById(id: Int): MapRegionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: MapRegionEntity): Long

    @Query("UPDATE map_regions SET sizeBytes = :sizeBytes WHERE offlineRegionId = :offlineRegionId")
    suspend fun updateSize(offlineRegionId: Long, sizeBytes: Long)

    @Query("SELECT offlineRegionId FROM map_regions")
    suspend fun getKnownRegionIds(): List<Long>

    @Delete
    suspend fun deleteRegion(region: MapRegionEntity)
}
