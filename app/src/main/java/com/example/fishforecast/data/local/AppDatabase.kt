package com.example.fishforecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.dao.MapRegionDao
import com.example.fishforecast.data.local.dao.WeatherDao
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.MapRegionEntity
import com.example.fishforecast.data.local.entities.WeatherEntity

@Database(
    entities = [FishEntity::class, WeatherEntity::class, MapRegionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
    abstract fun weatherDao(): WeatherDao
    abstract fun mapRegionDao(): MapRegionDao

    companion object {
        /** Офлайн-области (Фаза 3). Справочник рыб пересоздавать нельзя — он правится вручную. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `map_regions` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `name` TEXT NOT NULL,
                        `offlineRegionId` INTEGER NOT NULL,
                        `north` REAL NOT NULL,
                        `south` REAL NOT NULL,
                        `east` REAL NOT NULL,
                        `west` REAL NOT NULL,
                        `minZoom` REAL NOT NULL,
                        `maxZoom` REAL NOT NULL,
                        `sizeBytes` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
