package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_forecast")
data class WeatherEntity(
    @PrimaryKey
    val time: String, // ISO8601 string
    val temperature: Double,
    val humidity: Double,
    val pressure: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)