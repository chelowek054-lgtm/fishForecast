package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.SavedMapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMapDao {

    @Query("SELECT * FROM saved_maps ORDER BY createdAt DESC")
    fun getRegions(): Flow<List<SavedMapEntity>>

    @Query("SELECT * FROM saved_maps WHERE id = :id")
    suspend fun getRegionById(id: Int): SavedMapEntity?

    @Query("SELECT * FROM saved_maps WHERE id = :id")
    fun observeMap(id: Int): Flow<SavedMapEntity?>

    /** Первая по времени создания — ею подменяется выбор, если активной нет. */
    @Query("SELECT * FROM saved_maps ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestMap(): SavedMapEntity?

    @Query("UPDATE saved_maps SET name = :name WHERE id = :id")
    suspend fun rename(id: Int, name: String)

    @Query("UPDATE saved_maps SET normalPressureMmHg = :normalPressureMmHg WHERE id = :id")
    suspend fun updateNormalPressure(id: Int, normalPressureMmHg: Double?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: SavedMapEntity): Long

    @Query("UPDATE saved_maps SET sizeBytes = :sizeBytes WHERE offlineRegionId = :offlineRegionId")
    suspend fun updateSize(offlineRegionId: Long, sizeBytes: Long)

    @Query("SELECT offlineRegionId FROM saved_maps")
    suspend fun getKnownRegionIds(): List<Long>

    @Delete
    suspend fun deleteRegion(region: SavedMapEntity)
}
