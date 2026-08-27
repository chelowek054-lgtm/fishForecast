package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.SectorEntity
import com.example.fishforecast.data.local.entities.ZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {

    @Query("SELECT * FROM zones WHERE mapUid = :mapUid ORDER BY createdAt")
    fun getZones(mapUid: String): Flow<List<ZoneEntity>>

    @Query("SELECT * FROM zones WHERE mapUid = :mapUid ORDER BY createdAt")
    suspend fun zonesOf(mapUid: String): List<ZoneEntity>

    @Query("SELECT * FROM zone_sectors WHERE zoneUid IN (:zoneUids) ORDER BY name")
    fun getSectors(zoneUids: List<String>): Flow<List<SectorEntity>>

    @Query("SELECT * FROM zone_sectors WHERE zoneUid IN (:zoneUids) ORDER BY name")
    suspend fun sectorsOf(zoneUids: List<String>): List<SectorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<ZoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectors(sectors: List<SectorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: ZoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSector(sector: SectorEntity)

    @Delete
    suspend fun deleteZone(zone: ZoneEntity)

    @Query("DELETE FROM zone_sectors WHERE zoneUid = :zoneUid")
    suspend fun deleteSectorsOf(zoneUid: String)
}
