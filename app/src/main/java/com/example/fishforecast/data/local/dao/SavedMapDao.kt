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

    /** Поиск по глобальному идентификатору: так узнаётся чужой район. */
    @Query("SELECT * FROM saved_maps WHERE uid = :uid")
    suspend fun getRegionByUid(uid: String): SavedMapEntity?

    @Query("SELECT * FROM saved_maps WHERE id = :id")
    fun observeMap(id: Int): Flow<SavedMapEntity?>

    /** Первая по времени создания — ею подменяется выбор, если активной нет. */
    @Query("SELECT * FROM saved_maps ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestMap(): SavedMapEntity?

    @Query("UPDATE saved_maps SET name = :name WHERE id = :id")
    suspend fun rename(id: Int, name: String)

    @Query("UPDATE saved_maps SET shallowDepthM = :shallow, deepDepthM = :deep WHERE id = :id")
    suspend fun updateDepths(id: Int, shallow: Double?, deep: Double?)

    @Query("UPDATE saved_maps SET waterTempC = :temperature, waterTempAt = :measuredAt WHERE id = :id")
    suspend fun updateWaterMeasurement(id: Int, temperature: Double?, measuredAt: String?)

    @Query(
        "UPDATE saved_maps SET baselinePressureMmHg = :baselineMmHg, elevationM = :elevationM " +
            "WHERE id = :id"
    )
    suspend fun updateBaselinePressure(id: Int, baselineMmHg: Double?, elevationM: Double?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: SavedMapEntity): Long

    @Query("UPDATE saved_maps SET sizeBytes = :sizeBytes WHERE offlineRegionId = :offlineRegionId")
    suspend fun updateSize(offlineRegionId: Long, sizeBytes: Long)

    @Query("SELECT offlineRegionId FROM saved_maps")
    suspend fun getKnownRegionIds(): List<Long>

    @Delete
    suspend fun deleteRegion(region: SavedMapEntity)
}
