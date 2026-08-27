package com.example.fishforecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fishforecast.data.local.dao.CatchDao
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.dao.FishingSpotDao
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.dao.WeatherDao
import com.example.fishforecast.data.local.entities.CatchEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity

@Database(
    entities = [
        FishEntity::class,
        WeatherEntity::class,
        SavedMapEntity::class,
        FishingSpotEntity::class,
        CatchEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
    abstract fun weatherDao(): WeatherDao
    abstract fun savedMapDao(): SavedMapDao
    abstract fun fishingSpotDao(): FishingSpotDao
    abstract fun catchDao(): CatchDao

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

        /** Секретные точки (Фаза 3, шаг 3). */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `fishing_spots` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `name` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `fishId` INTEGER,
                        `note` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`fishId`) REFERENCES `fish`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fishing_spots_fishId` ON `fishing_spots` (`fishId`)")
            }
        }

        /** Журнал трофеев (Фаза 5). */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `catches` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `fishId` INTEGER,
                        `spotId` INTEGER,
                        `photoPath` TEXT,
                        `weightGrams` INTEGER,
                        `lengthCm` INTEGER,
                        `note` TEXT NOT NULL,
                        `caughtAt` INTEGER NOT NULL,
                        `temperature` REAL,
                        `pressureMmHg` REAL,
                        `windSpeed` REAL,
                        `biteScore` INTEGER,
                        FOREIGN KEY(`fishId`) REFERENCES `fish`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`spotId`) REFERENCES `fishing_spots`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_catches_fishId` ON `catches` (`fishId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_catches_spotId` ON `catches` (`spotId`)")
            }
        }

        /**
         * Направление ветра и норма давления водоёма: без них нельзя отличить
         * движение давления к норме от ухода в сторону, а прогрев воды —
         * от её перемешивания.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `weather_forecast` ADD COLUMN `windDirection` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `fishing_spots` ADD COLUMN `normalPressureMmHg` REAL")
            }
        }

        /**
         * Офлайн-области становятся сохранёнными картами — контекстом всего
         * приложения. Прогноз получает привязку к карте: у каждой свой, иначе
         * переключение района затирало бы данные предыдущего.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_maps` (
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
                        `normalPressureMmHg` REAL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `saved_maps` (
                        `id`, `name`, `offlineRegionId`, `north`, `south`, `east`, `west`,
                        `minZoom`, `maxZoom`, `sizeBytes`, `normalPressureMmHg`, `createdAt`
                    )
                    SELECT `id`, `name`, `offlineRegionId`, `north`, `south`, `east`, `west`,
                           `minZoom`, `maxZoom`, `sizeBytes`, NULL, `createdAt`
                    FROM `map_regions`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `map_regions`")

                // Прогноз — кэш, переносить его незачем: он перезагрузится
                // для активной карты при первом же открытии погоды.
                db.execSQL("DROP TABLE IF EXISTS `weather_forecast`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weather_forecast` (
                        `mapId` INTEGER NOT NULL,
                        `time` TEXT NOT NULL,
                        `temperature` REAL NOT NULL,
                        `humidity` REAL NOT NULL,
                        `pressure` REAL NOT NULL,
                        `windSpeed` REAL NOT NULL,
                        `windDirection` REAL NOT NULL DEFAULT 0.0,
                        `weatherCode` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`mapId`, `time`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Вероятность осадков. Старым строкам достаётся ноль — это кэш,
         * он всё равно перезапрашивается при первом обновлении погоды.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `weather_forecast` ADD COLUMN `precipitationChance` REAL NOT NULL DEFAULT 0.0"
                )
            }
        }
    }
}
