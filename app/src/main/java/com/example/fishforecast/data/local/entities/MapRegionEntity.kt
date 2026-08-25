package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Офлайн-область: рамка, которую рыболов оставил на экране, плюс диапазон
 * масштабов. Сами тайлы лежат в базе MapLibre, здесь — только описание,
 * по которому область показывают в списке и находят в OfflineManager.
 */
@Entity(tableName = "map_regions")
data class MapRegionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    /** Идентификатор региона в OfflineManager MapLibre. */
    val offlineRegionId: Long,
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
    val minZoom: Double,
    val maxZoom: Double,
    val sizeBytes: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
