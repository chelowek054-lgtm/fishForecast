package com.example.fishforecast.di

import android.content.Context
import androidx.room.Room
import com.example.fishforecast.data.local.AppDatabase
import com.example.fishforecast.data.local.dao.CatchDao
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.dao.FishingSpotDao
import com.example.fishforecast.data.local.dao.PressureLogDao
import com.example.fishforecast.data.local.dao.ZoneDao
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.dao.WeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fish_forecast_db"
        ).addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12,
            AppDatabase.MIGRATION_12_13,
            AppDatabase.MIGRATION_13_14,
            AppDatabase.MIGRATION_14_15
        ).build()
    }

    @Provides
    fun provideFishDao(database: AppDatabase): FishDao {
        return database.fishDao()
    }

    @Provides
    fun provideWeatherDao(database: AppDatabase): WeatherDao {
        return database.weatherDao()
    }

    @Provides
    fun provideZoneDao(database: AppDatabase): ZoneDao {
        return database.zoneDao()
    }

    @Provides
    fun providePressureLogDao(database: AppDatabase): PressureLogDao {
        return database.pressureLogDao()
    }

    @Provides
    fun provideSavedMapDao(database: AppDatabase): SavedMapDao {
        return database.savedMapDao()
    }

    @Provides
    fun provideFishingSpotDao(database: AppDatabase): FishingSpotDao {
        return database.fishingSpotDao()
    }

    @Provides
    fun provideCatchDao(database: AppDatabase): CatchDao {
        return database.catchDao()
    }
}