package com.example.fishforecast.data.repository

import android.content.Context
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.entities.FishEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FishRepository @Inject constructor(
    private val fishDao: FishDao,
    @ApplicationContext private val context: Context
) {
    fun getAllFish(): Flow<List<FishEntity>> = fishDao.getAllFish()

    suspend fun getFishById(id: Int): FishEntity? = fishDao.getFishById(id)

    suspend fun preloadDataIfNeeded(): Result<Unit> {
        return try {
            val currentFish = fishDao.getAllFish().first()
            if (currentFish.isEmpty()) {
                val jsonString = context.assets.open("initial_fish.json")
                    .bufferedReader()
                    .use { it.readText() }
                val fishList = Json.decodeFromString(ListSerializer(FishEntity.serializer()), jsonString)
                fishDao.insertAll(fishList)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertFish(fish: FishEntity) = fishDao.insertFish(fish)
    
    suspend fun deleteFish(fish: FishEntity) = fishDao.deleteFish(fish)
}