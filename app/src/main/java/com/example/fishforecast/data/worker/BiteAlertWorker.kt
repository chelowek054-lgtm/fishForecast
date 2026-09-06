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
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.fishforecast.MainActivity
import com.example.fishforecast.R
import com.example.fishforecast.data.local.AlertStore
import com.example.fishforecast.data.repository.FishRepository
import com.example.fishforecast.data.repository.FishingContextRepository
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.alert.AlertDecision
import com.example.fishforecast.domain.alert.decideAlert
import com.example.fishforecast.domain.alert.travelTime
import com.example.fishforecast.domain.bite.FindBiteWindowUseCase
import com.example.fishforecast.domain.location.LocationTracker
import com.example.fishforecast.domain.water.WaterState
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
    private val fishingContext: FishingContextRepository,
    private val findBiteWindow: FindBiteWindowUseCase,
    private val alertStore: AlertStore,
    private val locationTracker: LocationTracker
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val map = fishingContext.currentMap() ?: return Result.success()
        val fishList = fishRepository.getAllFish().first()
        val forecast = fishingContext.activeForecast.first()
        if (fishList.isEmpty() || forecast.isEmpty()) return Result.success()

        val now = LocalDateTime.now()
        val water = fishingContext.currentWater()
        val sunTimes = fishingContext.activeSunTimes.first()

        val window = findBiteWindow(
            fishList = fishList,
            forecast = forecast,
            from = now,
            normalPressureMmHg = map.baselinePressureMmHg,
            water = water,
            sunTimes = sunTimes
        ) ?: return Result.success()

        val windowTime = runCatching { LocalDateTime.parse(window.forecast.time) }.getOrNull()
            ?: return Result.success()

        // Сколько ехать. Без разрешения на геолокацию узнать неоткуда —
        // тогда политика берёт осторожный запас сама.
        val travel = locationTracker.getCurrentLocation()?.let { here ->
            travelTime(
                fromLatitude = here.latitude,
                fromLongitude = here.longitude,
                toLatitude = map.centerLatitude,
                toLongitude = map.centerLongitude
            )
        }

        val decision = decideAlert(
            windowTime = windowTime,
            windowScore = window.forecast.score,
            currentScore = currentScore(window.fish, forecast, map, water, sunTimes, now),
            now = now,
            travel = travel,
            history = alertStore.history.first()
        )

        if (decision !is AlertDecision.Notify) return Result.success()

        notify(
            title = "${window.fish.name}: клёв ${window.forecast.score} из 100",
            text = describe(window, windowTime, decision)
        )
        alertStore.remember(notifiedAt = now, windowTime = window.forecast.time)

        return Result.success()
    }

    /**
     * Балл ближайшего часа для той же рыбы: с ним и сравнивается окно.
     *
     * Без этого сравнения приложение звало бы «клёв 80» в день, когда и сейчас
     * восемьдесят, — то есть беспокоило бы без новости.
     */
    private fun currentScore(
        fish: FishEntity,
        forecast: List<WeatherEntity>,
        map: SavedMapEntity,
        water: WaterState,
        sunTimes: List<DailySunEntity>,
        now: LocalDateTime
    ): Int? = findBiteWindow.scoreAt(
        fish = fish,
        forecast = forecast,
        at = now,
        normalPressureMmHg = map.baselinePressureMmHg,
        water = water,
        sunTimes = sunTimes
    )

    /** Что написать: во сколько, через сколько выезжать и почему стоит. */
    private fun describe(
        window: com.example.fishforecast.domain.bite.BiteWindow,
        windowTime: LocalDateTime,
        decision: AlertDecision.Notify
    ): String = buildString {
        append("В ").append(windowTime.format(TIME_FORMAT))
        decision.travel?.let { travel ->
            val leave = windowTime.minus(travel)
            append(", выезжать около ").append(leave.format(TIME_FORMAT))
            append(" — ехать ").append(travel.toMinutes()).append(" мин")
        }
        append(". ")
        // Самый весомый фактор объясняет, почему стоит ехать.
        append(window.forecast.factors.maxByOrNull { it.value * it.weight }?.comment.orEmpty())
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
                // Не DEFAULT: всплывающее окно поверх экрана для прогноза
                // погоды — перебор. Уведомление ждёт в шторке, пока его не
                // откроют, и это ровно та настойчивость, которой оно стоит.
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Зовём на воду, когда условия складываются и вы успеваете доехать"
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
                .setSmallIcon(R.drawable.ic_notification)
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
        /**
         * Как часто заглядывать в прогноз.
         *
         * Чаще незачем: политика всё равно зовёт не больше раза в двенадцать
         * часов, а проверка в тихие часы уходит впустую.
         */
        private const val CHECK_INTERVAL_HOURS = 3L

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

            // KEEP, а не REPLACE, и без разовой проверки при запуске. Раньше
            // приложение проверяло прогноз на каждом открытии и звало на воду
            // человека, который уже держит это приложение в руках.
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
