package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.domain.water.WaterState
import com.example.fishforecast.domain.water.oxygenLevel
import com.example.fishforecast.domain.water.oxygenLevelText
import com.example.fishforecast.domain.water.oxygenSaturationMgL
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Считает активность конкретной рыбы по почасовому прогнозу.
 *
 * Модель опирается на три связанных фактора: давление, кислород и
 * температуру. Давление действует на плавательный пузырь, поэтому рыба
 * реагирует на отклонение от привычного ей фона и на то, куда это
 * отклонение движется. Кислород обратно связан с температурой воды:
 * прогрелась — стало нечем дышать, остыла — рыба ожила. Поэтому важна не
 * только сама температура, но и её ход.
 *
 * Справочник рыб задан в мм рт. ст., а Open-Meteo отдаёт гПа, поэтому весь
 * расчёт ведётся в мм рт. ст., а конвертация происходит на входе — иначе
 * пороги рыбы и данные погоды сравнивались бы в разных единицах.
 */
class CalculateFishActivityUseCase @Inject constructor() {

    /**
     * [normalPressureMmHg] — норма давления конкретного водоёма, посчитанная
     * по наблюдениям за место или по его высоте над уровнем моря. Пока её
     * нет, давление и тенденция не оцениваются: отклонение не от чего
     * считать. Подставлять сюда диапазон рыбы нельзя — норма это свойство
     * места, рыба к ней отношения не имеет.
     */
    operator fun invoke(
        fish: FishEntity,
        forecast: List<WeatherEntity>,
        normalPressureMmHg: Double? = null,
        water: WaterState? = null
    ): List<BiteForecast> {
        val sorted = forecast.sortedBy { it.time }
        val normal = normalPressureMmHg

        return sorted.mapIndexed { index, hour ->
            // Рыба живёт в воде, а не в воздухе. Пока вода не посчитана,
            // остаётся прежнее допущение — но оно именно допущение.
            val waterNow = water?.shallowAt(hour.time)
            val waterBefore = water?.let { state ->
                sorted.getOrNull(index - TREND_WINDOW_HOURS)?.let { state.shallowAt(it.time) }
            }

            val temperature = temperatureFactor(waterNow ?: hour.temperature, fish, waterNow != null)
            val pressure = pressureFactor(hour.pressure.hPaToMmHg(), normal)
            val trend = pressureTrendFactor(sorted, index, normal)
            val oxygen = if (waterNow != null) {
                waterOxygenFactor(waterNow, waterBefore, fish)
            } else {
                oxygenFactor(sorted, index, hour, fish)
            }
            val wind = windFactor(hour.windSpeed)

            val factors = listOf(temperature, oxygen, pressure, trend, wind)

            // Ограничители перемножаются: непригодную для рыбы воду не
            // компенсирует ни давление, ни ветер. Условия клёва складываются
            // с весами и лишь масштабируют то, что осталось возможным.
            val habitat = factors.filter { it.limiting }.fold(1.0) { acc, f -> acc * f.value }
            val conditions = factors.filterNot { it.limiting }.sumOf { it.value * it.weight }
            val score = (habitat * conditions * 100).roundToInt().coerceIn(0, 100)

            BiteForecast(
                time = hour.time,
                score = score,
                level = BiteLevel.fromScore(score),
                factors = factors
            )
        }
    }

    private fun temperatureFactor(
        temperature: Double,
        fish: FishEntity,
        fromWater: Boolean
    ): BiteFactor {
        val distance = distanceOutside(temperature, fish.minTemp.toDouble(), fish.maxTemp.toDouble())
        val value = falloff(distance, TEMPERATURE_TOLERANCE)

        val what = if (fromWater) "Вода" else "Воздух"
        return BiteFactor(
            name = if (fromWater) "Температура воды" else "Температура",
            value = value,
            weight = 0.0,
            limiting = true,
            comment = "$what ${temperature.roundToInt()}°C — " + when {
                distance == 0.0 -> "в комфортном диапазоне"
                temperature < fish.minTemp -> "холоднее комфорта"
                else -> "теплее комфорта"
            }
        )
    }

    /** Отклонение от нормы водоёма важнее абсолютного значения. */
    private fun pressureFactor(
        pressureMmHg: Double,
        normal: Double?
    ): BiteFactor {
        // Без нормы места отклонение не от чего считать. Честнее не
        // оценивать фактор вовсе, чем выдумать точку отсчёта.
        if (normal == null) {
            return BiteFactor(
                name = "Давление",
                value = 1.0,
                weight = WEIGHT_PRESSURE,
                comment = "${pressureMmHg.roundToInt()} мм рт. ст. — норма места " +
                    "ещё не посчитана, нужна сеть"
            )
        }

        val deviation = abs(pressureMmHg - normal)
        val value = falloff(deviation, PRESSURE_TOLERANCE)

        return BiteFactor(
            name = "Давление",
            value = value,
            weight = WEIGHT_PRESSURE,
            comment = "${pressureMmHg.roundToInt()} мм рт. ст. " + when {
                deviation <= AT_NORMAL_MMHG -> "— норма водоёма"
                pressureMmHg < normal -> "— ниже нормы на ${deviation.roundToInt()}"
                else -> "— выше нормы на ${deviation.roundToInt()}"
            }
        )
    }

    /**
     * Куда движется давление относительно нормы. Возврат к норме рыба
     * встречает оживлением, уход в сторону — наоборот; поэтому одинаковый по
     * величине скачок оценивается по-разному в зависимости от направления.
     * Пока истории нет, фактор не штрафует — иначе первые часы прогноза
     * выглядели бы хуже, чем есть.
     */
    private fun pressureTrendFactor(
        forecast: List<WeatherEntity>,
        index: Int,
        normal: Double?
    ): BiteFactor {
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
        val change = abs(current - previous)
        // Без нормы остаётся только величина скачка: куда движется
        // давление — к привычному фону или от него — сказать нечем.
        val deviationNow = normal?.let { abs(current - it) }
        val deviationBefore = normal?.let { abs(previous - it) }
        val movingToNormal = deviationNow != null && deviationBefore != null &&
            deviationNow < deviationBefore

        val value = when {
            change <= STABLE_PRESSURE_MMHG && (deviationNow ?: 0.0) <= AT_NORMAL_MMHG -> 1.0
            change <= STABLE_PRESSURE_MMHG -> 0.8
            movingToNormal -> 0.9
            change <= NOTICEABLE_PRESSURE_MMHG -> 0.5
            else -> 0.2
        }

        return BiteFactor(
            name = "Тенденция",
            value = value,
            weight = WEIGHT_TREND,
            comment = when {
                change <= STABLE_PRESSURE_MMHG -> "Давление стабильно за 3 часа"
                normal == null -> "Меняется на ${change.roundToInt()} мм за 3 часа"
                movingToNormal -> "Возвращается к норме (${change.roundToInt()} мм за 3 часа)"
                current > previous -> "Уходит вверх от нормы на ${change.roundToInt()} мм"
                else -> "Уходит вниз от нормы на ${change.roundToInt()} мм"
            }
        )
    }

    /**
     * Кислород напрямую не измеряется, поэтому оценивается косвенно: тёплая
     * вода удерживает его хуже, а похолодание после жары насыщает воду —
     * именно тогда рыба и оживает.
     *
     * Потребность зависит от вида: в одной и той же прогретой воде
     * теплолюбивой рыбе ещё комфортно, а холодолюбивой уже нечем дышать.
     * Отсчёт идёт от верхней границы её комфорта.
     */
    private fun oxygenFactor(
        forecast: List<WeatherEntity>,
        index: Int,
        hour: WeatherEntity,
        fish: FishEntity
    ): BiteFactor {
        val warmWaterStarts = fish.maxTemp - OXYGEN_MARGIN
        val warmthPenalty = falloff(
            distance = (hour.temperature - warmWaterStarts).coerceAtLeast(0.0),
            tolerance = HEAT_TOLERANCE
        )

        val previousIndex = index - TREND_WINDOW_HOURS
        val cooling = if (previousIndex >= 0) {
            hour.temperature - forecast[previousIndex].temperature
        } else {
            0.0
        }

        // Остывание добавляет кислорода, дальнейший прогрев — отнимает.
        val coolingBonus = when {
            cooling <= -NOTICEABLE_COOLING -> COOLING_BONUS
            cooling >= NOTICEABLE_COOLING -> -COOLING_BONUS
            else -> 0.0
        }

        val value = (warmthPenalty + coolingBonus).coerceIn(0.0, 1.0)

        return BiteFactor(
            name = "Кислород",
            value = value,
            weight = 0.0,
            limiting = true,
            comment = when {
                cooling <= -NOTICEABLE_COOLING -> "Вода остывает — кислорода прибавляется"
                hour.temperature > warmWaterStarts ->
                    "Для этой рыбы вода тёплая, кислорода меньше"
                cooling >= NOTICEABLE_COOLING -> "Продолжает греться — кислорода меньше"
                else -> "Кислорода достаточно"
            }
        )
    }

    /**
     * Кислород по температуре воды.
     *
     * Растворимость — функция температуры, и теперь её можно назвать в
     * мг/л, а не описывать словами. Но потолок растворимости ещё не
     * комфорт: с прогревом растёт и потребность самой рыбы, поэтому отсчёт
     * идёт от верхней границы её диапазона — в одной и той же воде амуру
     * привольно, а карпу уже нечем дышать.
     *
     * Остывание идёт в плюс не само по себе, а потому что вместе с ним в
     * воду приходит кислород.
     */
    private fun waterOxygenFactor(
        waterNow: Double,
        waterBefore: Double?,
        fish: FishEntity
    ): BiteFactor {
        val oxygen = oxygenSaturationMgL(waterNow)
        val warmWaterStarts = fish.maxTemp - OXYGEN_MARGIN
        val warmthPenalty = falloff(
            distance = (waterNow - warmWaterStarts).coerceAtLeast(0.0),
            tolerance = HEAT_TOLERANCE
        )

        val cooling = waterBefore?.let { it - waterNow } ?: 0.0
        val coolingBonus = when {
            cooling >= WATER_COOLING_STEP -> COOLING_BONUS
            cooling <= -WATER_COOLING_STEP -> -COOLING_BONUS
            else -> 0.0
        }

        return BiteFactor(
            name = "Кислород",
            value = (warmthPenalty + coolingBonus).coerceIn(0.0, 1.0),
            weight = 0.0,
            limiting = true,
            comment = "%.1f мг/л — %s".format(oxygen, oxygenLevelText(oxygenLevel(oxygen))) +
                when {
                    cooling >= WATER_COOLING_STEP -> ", вода остывает"
                    cooling <= -WATER_COOLING_STEP -> ", вода прогревается"
                    waterNow > warmWaterStarts -> ", для этой рыбы вода тёплая"
                    else -> ""
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
                windSpeedKmh <= STRONG_WIND_KMH -> " — заметный ветер, вода перемешивается"
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
        // Веса условий клёва в сумме дают единицу: они распределяют то, что
        // осталось после ограничителей среды.
        const val WEIGHT_PRESSURE = 0.45
        const val WEIGHT_TREND = 0.35
        const val WEIGHT_WIND = 0.20

        /** За сколько градусов от комфорта активность падает до нуля. */
        const val TEMPERATURE_TOLERANCE = 8.0

        /** Отклонение от нормы водоёма, при котором активность падает до нуля. */
        const val PRESSURE_TOLERANCE = 12.0

        /** Отклонение, которое рыба ещё считает нормой. */
        const val AT_NORMAL_MMHG = 2.0

        const val TREND_WINDOW_HOURS = 3
        const val STABLE_PRESSURE_MMHG = 1.0
        const val NOTICEABLE_PRESSURE_MMHG = 3.0

        /**
         * За сколько градусов до верхней границы комфорта рыба начинает
         * испытывать нехватку кислорода в прогретой воде.
         */
        const val OXYGEN_MARGIN = 4.0
        const val HEAT_TOLERANCE = 12.0
        const val NOTICEABLE_COOLING = 2.0

        /** Насколько должна сдвинуться вода за окно, чтобы это было ходом. */
        const val WATER_COOLING_STEP = 0.3
        const val COOLING_BONUS = 0.3

        const val LIGHT_WIND_KMH = 15.0
        const val STRONG_WIND_KMH = 30.0
    }
}
