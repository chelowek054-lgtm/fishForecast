package com.example.fishforecast.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fishforecast.data.local.dao.CatchDao
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.dao.FishingSpotDao
import com.example.fishforecast.data.local.dao.PressureLogDao
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.dao.WeatherDao
import com.example.fishforecast.data.local.entities.CatchEntity
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.PressureLogEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity

@Database(
    entities = [
        FishEntity::class,
        WeatherEntity::class,
        SavedMapEntity::class,
        FishingSpotEntity::class,
        CatchEntity::class,
        DailySunEntity::class,
        PressureLogEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
    abstract fun weatherDao(): WeatherDao
    abstract fun savedMapDao(): SavedMapDao
    abstract fun fishingSpotDao(): FishingSpotDao
    abstract fun catchDao(): CatchDao
    abstract fun pressureLogDao(): PressureLogDao

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

        /**
         * Фаза луны убрана из справочника. Влияние на пресноводную рыбу
         * спорное, в расчёт клёва поле никогда не входило и заполнялось
         * заглушкой — параметром оно только притворялось.
         *
         * Справочник правится вручную, поэтому таблица не пересоздаётся с
         * нуля, а переливается: пользовательские виды должны пережить
         * обновление.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `fish_new` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `minTemp` REAL NOT NULL,
                        `maxTemp` REAL NOT NULL,
                        `minPressure` REAL NOT NULL,
                        `maxPressure` REAL NOT NULL,
                        `imageUrl` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `fish_new` (
                        `id`, `name`, `description`, `minTemp`, `maxTemp`,
                        `minPressure`, `maxPressure`, `imageUrl`
                    )
                    SELECT `id`, `name`, `description`, `minTemp`, `maxTemp`,
                           `minPressure`, `maxPressure`, `imageUrl`
                    FROM `fish`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `fish`")
                db.execSQL("ALTER TABLE `fish_new` RENAME TO `fish`")
            }
        }

        /**
         * Погода начинает описывать воду, а не только воздух.
         *
         * Давление переезжает на станционное: приведённое к уровню моря не
         * сходится ни с барометром устройства, ни с нормой водоёма, которую
         * рыболов снял со своего прибора. Старые часы поэтому не переносятся
         * — в них лежит другая величина, а прогноз это кэш.
         *
         * Заодно появляются облачность, порывы, осадки в миллиметрах и приход
         * солнца: без них не посчитать прогрев воды. Восход и закат живут
         * отдельной таблицей — это свойство дня, а не часа.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `weather_forecast` ADD COLUMN `windGusts` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `weather_forecast` ADD COLUMN `precipitation` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `weather_forecast` ADD COLUMN `cloudCover` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `weather_forecast` ADD COLUMN `shortwaveRadiation` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("DELETE FROM `weather_forecast`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_sun` (
                        `mapId` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `sunrise` TEXT NOT NULL,
                        `sunset` TEXT NOT NULL,
                        PRIMARY KEY(`mapId`, `date`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Глубины района и замер воды. Без глубины температуру воды не
         * посчитать — именно она задаёт инерцию слоя, и из-за неё мелководье
         * остывает за ночь, а яма у дамбы не успевает.
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `saved_maps` ADD COLUMN `shallowDepthM` REAL")
                db.execSQL("ALTER TABLE `saved_maps` ADD COLUMN `deepDepthM` REAL")
                db.execSQL("ALTER TABLE `saved_maps` ADD COLUMN `waterTempC` REAL")
                db.execSQL("ALTER TABLE `saved_maps` ADD COLUMN `waterTempAt` TEXT")
            }
        }

        /**
         * История собственного барометра. Показания датчика точнее сетевого
         * прогноза и не требуют связи: они сняты там, где рыболов стоял.
         */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `barometer_log` (
                        `time` TEXT NOT NULL,
                        `pressure` REAL NOT NULL,
                        PRIMARY KEY(`time`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
