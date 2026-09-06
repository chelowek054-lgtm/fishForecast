package com.example.fishforecast

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.fishforecast.data.worker.BiteAlertWorker
import com.example.fishforecast.data.worker.WeatherSyncWorker
import org.maplibre.android.MapLibre
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // Планирование заданий открывает собственную базу WorkManager, то есть
        // лезет на диск. Первому кадру это не нужно: расписание не влияет ни на
        // что, что рыболов увидит в первую секунду.
        CoroutineScope(Dispatchers.Default).launch {
            WeatherSyncWorker.schedule(this@FishForecastApp)
            BiteAlertWorker.schedule(this@FishForecastApp)
        }
    }
}
