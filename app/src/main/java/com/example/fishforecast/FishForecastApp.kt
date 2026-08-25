package com.example.fishforecast

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.fishforecast.data.worker.WeatherSyncWorker
import org.maplibre.android.MapLibre
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FishForecastApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Инициализация нужна не только карте: офлайн-областями можно
        // управлять из «Хранилища», не открывая экран карты.
        MapLibre.getInstance(this)

        WeatherSyncWorker.schedule(this)
    }
}
