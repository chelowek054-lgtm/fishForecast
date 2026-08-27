package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.water.WaterState
import java.time.LocalDateTime
import javax.inject.Inject

/** Ближайшее окно хорошего клёва: какая рыба, во сколько и почему. */
data class BiteWindow(
    val fish: FishEntity,
    val forecast: BiteForecast
)

/**
 * Ищет повод позвать рыболова на воду.
 *
 * Уведомление имеет смысл, только если опережает событие, поэтому отсчёт
 * идёт со следующего часа. Из всех рыб справочника берётся лучшая: звать
 * стоит один раз и по самому весомому поводу, а не по каждой рыбе.
 */
class FindBiteWindowUseCase @Inject constructor(
    private val calculateFishActivity: CalculateFishActivityUseCase
) {

    operator fun invoke(
        fishList: List<FishEntity>,
        forecast: List<WeatherEntity>,
        from: LocalDateTime,
        lookaheadHours: Long = DEFAULT_LOOKAHEAD_HOURS,
        minimumScore: Int = DEFAULT_MINIMUM_SCORE,
        normalPressureMmHg: Double? = null,
        water: WaterState? = null,
        sunTimes: List<DailySunEntity> = emptyList()
    ): BiteWindow? {
        val until = from.plusHours(lookaheadHours)

        return fishList
            .flatMap { fish ->
                calculateFishActivity(fish, forecast, normalPressureMmHg, water, sunTimes)
                    .filter { it.score >= minimumScore }
                    .filter { hour ->
                        val time = hour.time.toLocalDateTimeOrNull() ?: return@filter false
                        time.isAfter(from) && !time.isAfter(until)
                    }
                    .map { BiteWindow(fish, it) }
            }
            .maxByOrNull { it.forecast.score }
    }

    /** Время приходит из чужого источника, поэтому разбор не должен падать. */
    private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
        runCatching { LocalDateTime.parse(this) }.getOrNull()

    private companion object {
        /** Ниже этого порога звать на воду незачем. */
        const val DEFAULT_MINIMUM_SCORE = 75
        const val DEFAULT_LOOKAHEAD_HOURS = 24L
    }
}
