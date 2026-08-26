package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Считает активность конкретной рыбы по почасовому прогнозу.
 *
 * Справочник рыб задан в мм рт. ст., а Open-Meteo отдаёт гПа, поэтому весь
 * расчёт ведётся в мм рт. ст., а конвертация происходит на входе — иначе
 * пороги рыбы и данные погоды сравнивались бы в разных единицах.
 */
class CalculateFishActivityUseCase @Inject constructor() {

    operator fun invoke(fish: FishEntity, forecast: List<WeatherEntity>): List<BiteForecast> {
        val sorted = forecast.sortedBy { it.time }

        return sorted.mapIndexed { index, hour ->
            val pressureMmHg = hour.pressure.hPaToMmHg()

            val temperature = temperatureFactor(hour.temperature, fish)
            val pressure = pressureFactor(pressureMmHg, fish)
            val trend = pressureTrendFactor(sorted, index)
            val wind = windFactor(hour.windSpeed)

            val factors = listOf(temperature, pressure, trend, wind)
            val score = (factors.sumOf { it.value * it.weight } * 100).roundToInt().coerceIn(0, 100)

            BiteForecast(
                time = hour.time,
                score = score,
                level = BiteLevel.fromScore(score),
                factors = factors
            )
        }
    }

    private fun temperatureFactor(temperature: Double, fish: FishEntity): BiteFactor {
        val distance = distanceOutside(temperature, fish.minTemp.toDouble(), fish.maxTemp.toDouble())
        val value = falloff(distance, TEMPERATURE_TOLERANCE)

        return BiteFactor(
            name = "Температура",
            value = value,
            weight = WEIGHT_TEMPERATURE,
            comment = when {
                distance == 0.0 -> "${temperature.roundToInt()}°C — в комфортном диапазоне"
                temperature < fish.minTemp -> "${temperature.roundToInt()}°C — холоднее комфорта"
                else -> "${temperature.roundToInt()}°C — теплее комфорта"
            }
        )
    }

    private fun pressureFactor(pressureMmHg: Double, fish: FishEntity): BiteFactor {
        val distance = distanceOutside(
            value = pressureMmHg,
            min = fish.minPressure.toDouble(),
            max = fish.maxPressure.toDouble()
        )
        val value = falloff(distance, PRESSURE_TOLERANCE)

        return BiteFactor(
            name = "Давление",
            value = value,
            weight = WEIGHT_PRESSURE,
            comment = "${pressureMmHg.roundToInt()} мм рт. ст." + when {
                distance == 0.0 -> " — привычное для рыбы"
                pressureMmHg < fish.minPressure -> " — ниже привычного"
                else -> " — выше привычного"
            }
        )
    }

    /**
     * Рыба реагирует не столько на само давление, сколько на его изменение:
     * при резком скачке она уходит на глубину и перестаёт брать. Сравнение
     * идёт с отметкой трёхчасовой давности; пока истории нет, фактор не
     * штрафует — иначе первые часы прогноза выглядели бы хуже, чем есть.
     */
    private fun pressureTrendFactor(forecast: List<WeatherEntity>, index: Int): BiteFactor {
        val previousIndex = index - TREND_WINDOW_HOURS
        if (previousIndex < 0) {
            return BiteFactor(
                name = "Тенденция",
                value = 1.0,
                weight = WEIGHT_TREND,
                comment = "Недостаточно истории для оценки"
            )
        }

        val current = forecast[index].pressure.hPaToMmHg()
        val previous = forecast[previousIndex].pressure.hPaToMmHg()
        val delta = current - previous
        val magnitude = abs(delta)

        val value = when {
            magnitude <= STABLE_PRESSURE_MMHG -> 1.0
            magnitude <= NOTICEABLE_PRESSURE_MMHG -> 0.6
            else -> 0.2
        }

        return BiteFactor(
            name = "Тенденция",
            value = value,
            weight = WEIGHT_TREND,
            comment = when {
                magnitude <= STABLE_PRESSURE_MMHG -> "Давление стабильно за 3 часа"
                delta > 0 -> "Растёт на ${magnitude.roundToInt()} мм за 3 часа"
                else -> "Падает на ${magnitude.roundToInt()} мм за 3 часа"
            }
        )
    }

    private fun windFactor(windSpeedKmh: Double): BiteFactor {
        val value = when {
            windSpeedKmh <= LIGHT_WIND_KMH -> 1.0
            windSpeedKmh <= STRONG_WIND_KMH -> 0.7
            else -> 0.3
        }

        return BiteFactor(
            name = "Ветер",
            value = value,
            weight = WEIGHT_WIND,
            comment = "${windSpeedKmh.roundToInt()} км/ч" + when {
                windSpeedKmh <= LIGHT_WIND_KMH -> " — рябь на воде, это в плюс"
                windSpeedKmh <= STRONG_WIND_KMH -> " — заметный ветер"
                else -> " — сильный ветер, рыбалка трудная"
            }
        )
    }

    /** На сколько значение выходит за границы диапазона; 0, если внутри. */
    private fun distanceOutside(value: Double, min: Double, max: Double): Double = when {
        value < min -> min - value
        value > max -> value - max
        else -> 0.0
    }

    /** Плавное затухание: у границы диапазона обрыва быть не должно. */
    private fun falloff(distance: Double, tolerance: Double): Double =
        (1.0 - distance / tolerance).coerceIn(0.0, 1.0)

    private companion object {
        const val WEIGHT_TEMPERATURE = 0.35
        const val WEIGHT_PRESSURE = 0.30
        const val WEIGHT_TREND = 0.25
        const val WEIGHT_WIND = 0.10

        /** За сколько градусов от комфорта активность падает до нуля. */
        const val TEMPERATURE_TOLERANCE = 8.0

        /** То же для давления, в мм рт. ст. */
        const val PRESSURE_TOLERANCE = 12.0

        const val TREND_WINDOW_HOURS = 3
        const val STABLE_PRESSURE_MMHG = 1.0
        const val NOTICEABLE_PRESSURE_MMHG = 3.0

        const val LIGHT_WIND_KMH = 15.0
        const val STRONG_WIND_KMH = 30.0
    }
}
