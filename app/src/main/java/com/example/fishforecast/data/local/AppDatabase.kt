package com.example.fishforecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.dao.WeatherDao
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity

@Database(
    entities = [FishEntity::class, WeatherEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
    abstract fun weatherDao(): WeatherDao
}