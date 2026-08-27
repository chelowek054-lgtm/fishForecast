package com.example.fishforecast.domain.weather

import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg

/**
 * Куда идёт давление в ближайшие часы.
 *
 * Для клёва важна не сама цифра, а движение: рыба реагирует на перемену,
 * а не на абсолютное значение. Порог в 1 мм рт. ст. отсекает шум прогноза.
 */
enum class PressureDirection { RISING, FALLING, STEADY }

data class PressureTrend(
    val direction: PressureDirection,
    /** Изменение за окно наблюдения, мм рт. ст. */
    val deltaMmHg: Double
)

private const val STEADY_THRESHOLD_MMHG = 1.0

/** @param hours ближайшие часы прогноза, по возрастанию времени. */
fun pressureTrend(hours: List<WeatherEntity>): PressureTrend? {
    if (hours.size < 2) return null

    val delta = hours.last().pressure.hPaToMmHg() - hours.first().pressure.hPaToMmHg()
    val direction = when {
        delta > STEADY_THRESHOLD_MMHG -> PressureDirection.RISING
        delta < -STEADY_THRESHOLD_MMHG -> PressureDirection.FALLING
        else -> PressureDirection.STEADY
    }
    return PressureTrend(direction, delta)
}
