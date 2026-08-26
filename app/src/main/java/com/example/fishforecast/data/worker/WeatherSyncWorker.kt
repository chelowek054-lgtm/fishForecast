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
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.data.repository.WeatherRepository
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
    private val fishingContext: FishingContextRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Прогноз обновляется для выбранного района, а не для того места, где
        // рыболов оказался с телефоном.
        val map = fishingContext.currentMap() ?: return Result.success()

        return fishingContext.refreshWeather().fold(
            onSuccess = {
                repository.cleanOldData(map.id)
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
