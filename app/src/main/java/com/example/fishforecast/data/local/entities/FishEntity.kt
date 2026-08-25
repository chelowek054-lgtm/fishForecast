package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "fish")
data class FishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val minTemp: Float,
    val maxTemp: Float,
    val minPressure: Float,
    val maxPressure: Float,
    val moonPhaseImpact: String, // e.g., "Full Moon", "New Moon", "None"
    val imageUrl: String? = null
)