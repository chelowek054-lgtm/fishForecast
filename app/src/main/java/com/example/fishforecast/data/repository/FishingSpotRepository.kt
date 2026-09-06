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

    /**
     * Точки района.
     *
     * По ссылке, а не по координатам: точка на стыке двух районов раньше
     * принадлежала обоим, а точка удалённого района — никому и навсегда.
     */
    fun spotsForMap(mapId: Int): Flow<List<FishingSpotEntity>> = dao.getSpotsForMap(mapId)

    /** Точки без района: наследство схемы, в которой района у точки не было. */
    val orphanSpots: Flow<List<FishingSpotEntity>> = dao.getOrphanSpots()

    suspend fun addSpot(spot: FishingSpotEntity) = dao.insertSpot(spot)

    suspend fun deleteSpot(spot: FishingSpotEntity) = dao.deleteSpot(spot)
}
