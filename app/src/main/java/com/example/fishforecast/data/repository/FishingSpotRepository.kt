package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.FishingSpotDao
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FishingSpotRepository @Inject constructor(
    private val dao: FishingSpotDao
) {
    val spots: Flow<List<FishingSpotEntity>> = dao.getSpots()

    suspend fun addSpot(spot: FishingSpotEntity) = dao.insertSpot(spot)

    suspend fun deleteSpot(spot: FishingSpotEntity) = dao.deleteSpot(spot)
}
