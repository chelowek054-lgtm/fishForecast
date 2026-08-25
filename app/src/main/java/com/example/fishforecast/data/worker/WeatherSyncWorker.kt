package com.example.fishforecast.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.fishforecast.data.repository.WeatherRepository
import com.example.fishforecast.domain.location.LocationTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Фоновое обновление прогноза: приложение должно оставаться полезным
 * без сети, поэтому кэш пополняется, пока связь ещё есть.
 */
@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val location = locationTracker.getCurrentLocation() ?: return Result.retry()

        return repository.fetchWeather(location.latitude, location.longitude).fold(
            onSuccess = {
                repository.cleanOldData()
                Result.success()
            },
            onFailure = { Result.retry() }
        )
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "weather_sync"
        private const val SYNC_INTERVAL_HOURS = 6L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
