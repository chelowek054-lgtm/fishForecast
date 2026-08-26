package com.example.fishforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fishforecast.data.local.entities.CatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatchDao {

    @Query("SELECT * FROM catches ORDER BY caughtAt DESC")
    fun getCatches(): Flow<List<CatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatch(entity: CatchEntity)

    @Delete
    suspend fun deleteCatch(entity: CatchEntity)
}
