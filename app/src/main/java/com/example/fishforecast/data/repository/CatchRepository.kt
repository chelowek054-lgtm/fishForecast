package com.example.fishforecast.data.repository

import android.app.Application
import com.example.fishforecast.data.local.dao.CatchDao
import com.example.fishforecast.data.local.entities.CatchEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase
import com.example.fishforecast.domain.sensor.hPaToMmHg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatchRepository @Inject constructor(
    private val application: Application,
    private val dao: CatchDao,
    private val fishingContext: FishingContextRepository,
    private val calculateFishActivity: CalculateFishActivityUseCase
) {
    val catches: Flow<List<CatchEntity>> = dao.getCatches()

    /**
     * Сохраняет улов, дописывая условия текущего часа. Прогноз в кэше
     * живёт недолго, поэтому погода и оценка клёва фиксируются здесь —
     * позже сверить предсказание с реальностью будет уже не по чему.
     */
    suspend fun addCatch(entity: CatchEntity, fish: FishEntity?) {
        val hour = currentForecastHour()
        // Норма берётся у активной карты: улов записывается там, где рыболов
        // сейчас ловит, и сверять прогноз потом нужно с той же нормой.
        val normalPressure = fishingContext.currentMap()?.normalPressureMmHg

        val biteScore = if (fish != null && hour != null) {
            val forecast = fishingContext.activeForecast.first()
            calculateFishActivity(fish, forecast, normalPressure)
                .firstOrNull { it.time == hour.time }?.score
        } else {
            null
        }

        dao.insertCatch(
            entity.copy(
                temperature = hour?.temperature,
                pressureMmHg = hour?.pressure?.hPaToMmHg(),
                windSpeed = hour?.windSpeed,
                biteScore = biteScore
            )
        )
    }

    suspend fun deleteCatch(entity: CatchEntity) {
        entity.photoPath?.let { path -> withContext(Dispatchers.IO) { File(path).delete() } }
        dao.deleteCatch(entity)
    }

    /** Файл для снимка: во внутренней памяти, чтобы фото не пропало из галереи чужими руками. */
    fun createPhotoFile(): File {
        val dir = File(application.filesDir, PHOTO_DIR).apply { mkdirs() }
        return File(dir, "catch_${System.currentTimeMillis()}.jpg")
    }

    /**
     * Прогноз разложен по целым часам, поэтому текущее время усекается:
     * иначе «08:25» никогда не совпадёт с «08:00» и улов останется без
     * погоды — ровно того, ради чего запись и делается.
     */
    private suspend fun currentForecastHour() =
        fishingContext.activeForecast.first()
            .firstOrNull { it.time == currentHour() }

    private fun currentHour(): String =
        LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).format(HOUR_FORMAT)

    private companion object {
        const val PHOTO_DIR = "catches"
        val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }
}
