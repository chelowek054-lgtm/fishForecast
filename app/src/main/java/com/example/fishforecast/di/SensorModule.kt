package com.example.fishforecast.di

import com.example.fishforecast.data.sensor.BarometerPressureProvider
import com.example.fishforecast.domain.sensor.PressureProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {

    @Binds
    @Singleton
    abstract fun bindPressureProvider(
        barometerPressureProvider: BarometerPressureProvider
    ): PressureProvider
}
