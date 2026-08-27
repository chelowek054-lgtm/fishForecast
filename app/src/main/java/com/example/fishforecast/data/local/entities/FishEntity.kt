package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "fish")
data class FishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * Глобальный идентификатор вида. Числовой ключ у каждого устройства
     * свой, а справочник ходит между рыболовами вместе с районом: по uid
     * видно, что это тот же карп, а не второй такой же.
     */
    val uid: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val minTemp: Float,
    val maxTemp: Float,
    val minPressure: Float,
    val maxPressure: Float,
    val imageUrl: String? = null
)