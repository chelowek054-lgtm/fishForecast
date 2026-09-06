package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FishingSpotDao {

    @Query("SELECT * FROM fishing_spots ORDER BY createdAt DESC")
    fun getSpots(): Flow<List<FishingSpotEntity>>

    /** Точки одного района: принадлежность теперь ссылка, а не геометрия. */
    @Query("SELECT * FROM fishing_spots WHERE mapId = :mapId ORDER BY createdAt DESC")
    fun getSpotsForMap(mapId: Int): Flow<List<FishingSpotEntity>>

    /** Точки, не привязанные ни к какому району: наследство прежней схемы. */
    @Query("SELECT * FROM fishing_spots WHERE mapId IS NULL ORDER BY createdAt DESC")
    fun getOrphanSpots(): Flow<List<FishingSpotEntity>>

    @Query("SELECT * FROM fishing_spots")
    suspend fun allSpots(): List<FishingSpotEntity>

    @Query("SELECT * FROM fishing_spots WHERE uid = :uid")
    suspend fun getSpotByUid(uid: String): FishingSpotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: FishingSpotEntity)

    @Delete
    suspend fun deleteSpot(spot: FishingSpotEntity)
}
