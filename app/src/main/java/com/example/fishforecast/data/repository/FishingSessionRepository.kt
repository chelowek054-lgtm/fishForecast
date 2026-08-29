package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.FishingSessionDao
import com.example.fishforecast.data.local.entities.FishingSessionEntity
import com.example.fishforecast.domain.bite.CalculateFishActivityUseCase
import com.example.fishforecast.domain.bite.PlaceContext
import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.light.lightPhaseAt
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.domain.session.FishingStrategy
import com.example.fishforecast.domain.session.HourContext
import com.example.fishforecast.domain.session.SessionConditions
import com.example.fishforecast.domain.session.SessionPlanInput
import com.example.fishforecast.domain.session.buildStrategy
import com.example.fishforecast.domain.weather.kmhToMs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Выезды: сборы, план и итог.
 *
 * Условия и план записываются в момент старта, а не собираются потом:
 * прогноз в кэше живёт неделю, а разбирать неудачный выезд рыболов будет
 * тогда, когда вспомнит — иногда через месяц.
 */
@Singleton
class FishingSessionRepository @Inject constructor(
    private val dao: FishingSessionDao,
    private val fishingContext: FishingContextRepository,
    private val knowledgeRepository: KnowledgeRepository,
    private val fishRepository: FishRepository,
    private val calculateFishActivity: CalculateFishActivityUseCase
) {
    val active: Flow<FishingSessionEntity?> = dao.observeActive()

    val sessions: Flow<List<FishingSessionEntity>> = dao.observeAll()

    /** Условия и план на сейчас: то, что увидит рыболов до старта. */
    suspend fun previewStrategy(input: SessionPlanInput): FishingStrategy {
        val conditions = currentConditions(input)
        return buildStrategy(input, conditions, knowledgeRepository.current())
    }

    /** Начинает выезд, записывая условия и план снимком. */
    suspend fun start(input: SessionPlanInput): Result<Int> = runCatching {
        require(dao.active() == null) { "Рыбалка уже идёт" }

        val conditions = currentConditions(input)
        val strategy = buildStrategy(input, conditions, knowledgeRepository.current())
        val map = fishingContext.currentMap()

        // Слой не спрашивают у рыболова — его выбрал план, и в архив
        // записывается именно то, что было советовано.
        val layer = conditions.hours.firstOrNull()
            ?.let { if (it.scoreDeep - it.scoreShallow >= 5) WaterLayerChoice.DEEP else null }
            ?: WaterLayerChoice.SHALLOW

        dao.insert(
            FishingSessionEntity(
                mapId = map?.id,
                targetFishId = input.fish.id,
                methodId = input.methodId,
                layer = layer.name,
                hasGroundbait = input.hasGroundbait,
                goal = input.goal.name,
                waterTempC = when (layer) {
                    WaterLayerChoice.SHALLOW -> conditions.waterShallowC
                    WaterLayerChoice.DEEP -> conditions.waterDeepC
                },
                oxygenMgL = conditions.oxygenMgL,
                pressureMmHg = conditions.hour?.pressure?.hPaToMmHg(),
                windMs = conditions.hour?.windSpeed?.kmhToMs(),
                lightPhase = conditions.lightPhase?.name,
                biteScore = conditions.forecast.firstOrNull()?.score,
                plan = strategy.asText()
            )
        ).toInt()
    }

    /** Закрывает выезд итогом: сколько поймал и как прошло. */
    suspend fun finish(
        caughtCount: Int?,
        note: String,
        rating: Int?
    ): Result<Unit> = runCatching {
        val session = dao.active() ?: error("Активной рыбалки нет")
        dao.update(
            session.copy(
                finishedAt = System.currentTimeMillis(),
                caughtCount = caughtCount,
                resultNote = note,
                rating = rating
            )
        )
    }

    suspend fun cancel(): Result<Unit> = runCatching {
        val session = dao.active() ?: return@runCatching
        dao.delete(session)
    }

    /**
     * Условия на ближайший час.
     *
     * Всё уже посчитано другими частями приложения — здесь только сбор в
     * одно место, чтобы план и снимок опирались на одни и те же числа.
     */
    private suspend fun currentConditions(input: SessionPlanInput): SessionConditions {
        val forecast = fishingContext.activeForecast.first()
        val water = fishingContext.currentWater()
        val sunTimes = fishingContext.activeSunTimes.first()
        val map = fishingContext.currentMap()

        val now = LocalDateTime.now()
        val hour = forecast.minByOrNull {
            kotlin.math.abs(Duration.between(LocalDateTime.parse(it.time), now).toMinutes())
        }

        val normal = fishingContext.normalPressureFor(map)

        // Считаем оба слоя: раскладка суток и есть сравнение мели с ямой
        // час за часом.
        fun scores(layer: WaterLayerChoice) = calculateFishActivity(
            fish = input.fish,
            forecast = forecast,
            normalPressureMmHg = normal,
            water = water,
            sunTimes = sunTimes,
            place = PlaceContext(layer = layer)
        ).associate { it.time to it.score }

        val shallowScores = scores(WaterLayerChoice.SHALLOW)
        val deepScores = scores(WaterLayerChoice.DEEP)

        val bite = hour?.let {
            calculateFishActivity(
                fish = input.fish,
                forecast = forecast,
                normalPressureMmHg = normal,
                water = water,
                sunTimes = sunTimes
            ).dropWhile { forecastHour -> forecastHour.time < it.time }
        }.orEmpty()

        val hours = hour?.let { current ->
            forecast
                .filter { it.time >= current.time }
                .map { entry ->
                    val moment = LocalDateTime.parse(entry.time)
                    HourContext(
                        time = entry.time,
                        phase = lightPhaseAt(
                            moment,
                            sunTimes.firstOrNull { day ->
                                day.date == moment.toLocalDate().toString()
                            }
                        ),
                        shallowC = water.shallowAt(entry.time),
                        deepC = water.deepAt(entry.time),
                        oxygenMgL = water.oxygenAt(entry.time),
                        scoreShallow = shallowScores[entry.time] ?: 0,
                        scoreDeep = deepScores[entry.time] ?: 0
                    )
                }
        }.orEmpty()

        // Муть после ливня держится примерно сутки — по ней подбирается
        // цвет приманки.
        val rain = forecast
            .filter {
                val time = LocalDateTime.parse(it.time)
                time.isAfter(now.minusDays(1)) && !time.isAfter(now)
            }
            .sumOf { it.precipitation }

        return SessionConditions(
            hour = hour,
            waterShallowC = hour?.let { water.shallowAt(it.time) },
            waterDeepC = hour?.let { water.deepAt(it.time) },
            oxygenMgL = hour?.let { water.oxygenAt(it.time) },
            lightPhase = hour?.let {
                val moment = LocalDateTime.parse(it.time)
                lightPhaseAt(
                    moment,
                    sunTimes.firstOrNull { day -> day.date == moment.toLocalDate().toString() }
                )
            },
            waterBodyId = map?.waterBodyType,
            forecast = bite,
            hours = hours,
            rainLastDayMm = rain
        )
    }

    /** Плоский текст плана: он должен читаться и через год, без кода. */
    private fun FishingStrategy.asText(): String = buildList {
        add("${place.title}: ${place.value} — ${place.reason}")
        add("${horizon.title}: ${horizon.value} — ${horizon.reason}")
        bait?.let { add("${it.title}: ${it.value} — ${it.reason}") }
        backupBait?.let { add("${it.title}: ${it.value}") }
        groundbait?.let { add("${it.title}: ${it.value} — ${it.reason}") }
        baiting?.let { add("${it.title}: ${it.value} — ${it.reason}") }
        selection?.let { add("${it.title}: ${it.value} — ${it.reason}") }
        rig?.let { add("${it.title}: ${it.value} — ${it.reason}") }
        window?.let { add("${it.title}: ${it.value} — ${it.reason}") }
        warnings.forEach { add("Важно: $it") }
    }.joinToString("\n")
}
