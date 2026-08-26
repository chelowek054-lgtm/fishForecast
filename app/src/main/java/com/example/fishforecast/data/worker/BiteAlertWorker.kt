package com.example.fishforecast.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.fishforecast.MainActivity
import com.example.fishforecast.R
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.WeatherRepository
import com.example.fishforecast.domain.bite.FindBiteWindowUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Зовёт на воду, когда в ближайшие сутки складываются условия. Проверка
 * идёт по уже скачанному прогнозу, поэтому работает и без сети.
 */
@HiltWorker
class BiteAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val fishRepository: FishRepository,
    private val weatherRepository: WeatherRepository,
    private val findBiteWindow: FindBiteWindowUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fishList = fishRepository.getAllFish().first()
        val forecast = weatherRepository.weatherForecast.first()
        if (fishList.isEmpty() || forecast.isEmpty()) return Result.success()

        val window = findBiteWindow(
            fishList = fishList,
            forecast = forecast,
            from = LocalDateTime.now()
        ) ?: return Result.success()

        notify(
            title = "${window.fish.name}: клёв ${window.forecast.score} из 100",
            text = buildString {
                append("В ")
                append(LocalDateTime.parse(window.forecast.time).format(TIME_FORMAT))
                append(". ")
                // Самый весомый фактор объясняет, почему стоит ехать.
                append(window.forecast.factors.maxByOrNull { it.value * it.weight }?.comment.orEmpty())
            }
        )

        return Result.success()
    }

    private fun notify(title: String, text: String) {
        // Без разрешения уведомление молча не покажется — проверяем явно,
        // чтобы не считать работу выполненной впустую.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Прогноз клёва",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Сообщения о том, что условия сложились удачно"
            }
        )

        val intent = android.content.Intent(context, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        )
    }

    companion object {
        private const val CHANNEL_ID = "bite_alerts"
        private const val NOTIFICATION_ID = 1001
        private const val UNIQUE_WORK_NAME = "bite_alert"
        private const val IMMEDIATE_WORK_NAME = "bite_alert_now"
        private const val CHECK_INTERVAL_HOURS = 6L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BiteAlertWorker>(
                CHECK_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(
                    // Сеть не нужна: считаем по уже скачанному прогнозу.
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            val manager = WorkManager.getInstance(context)
            manager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            // Периодическая работа впервые сработает только через интервал,
            // поэтому при запуске проверяем прогноз сразу: он мог обновиться
            // в фоне, пока приложение не открывали.
            manager.enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<BiteAlertWorker>().build()
            )
        }

        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
