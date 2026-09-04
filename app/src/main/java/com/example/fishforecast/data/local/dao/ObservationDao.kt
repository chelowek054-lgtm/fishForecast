package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Query("SELECT * FROM observations WHERE mapId = :mapId ORDER BY notedAt DESC")
    fun getObservations(mapId: Int): Flow<List<ObservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: ObservationEntity)

    @Delete
    suspend fun delete(observation: ObservationEntity)

    /**
     * Выдохшиеся отметки не нужны никому: срок у них короткий, а копятся они
     * с каждой рыбалки. Чистится по времени, а не по сроку из словаря, —
     * самый долгий срок там двенадцать часов, сутки с запасом перекрывают.
     */
    @Query("DELETE FROM observations WHERE notedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
