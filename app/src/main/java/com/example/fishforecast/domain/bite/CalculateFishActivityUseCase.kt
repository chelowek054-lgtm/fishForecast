package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.fish.decodeLightActivity
import com.example.fishforecast.domain.light.lightActivity
import com.example.fishforecast.domain.light.lightPhaseAt
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.domain.weather.kmhToMs
import java.time.LocalDateTime
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
        water: WaterState? = null,
        sunTimes: List<DailySunEntity> = emptyList(),
        place: PlaceContext = PlaceContext()
    ): List<BiteForecast> {
        val sorted = forecast.sortedBy { it.time }
        val normal = normalPressureMmHg

        return sorted.mapIndexed { index, hour ->
            // Рыба живёт в воде, а не в воздухе. Пока вода не посчитана,
            // остаётся прежнее допущение — но оно именно допущение.
            val waterNow = water?.layerAt(hour.time, place.layer)?.plus(place.waterOffsetC)
            val waterBefore = water?.let { state ->
                sorted.getOrNull(index - TREND_WINDOW_HOURS)
                    ?.let { state.layerAt(it.time, place.layer) }
                    ?.plus(place.waterOffsetC)
            }

            val temperature = temperatureFactor(waterNow ?: hour.temperature, fish, waterNow != null)
            val pressure = pressureFactor(hour.pressure.hPaToMmHg(), normal)
            val trend = pressureTrendFactor(sorted, index, normal)
            val oxygen = if (waterNow != null) {
                waterOxygenFactor(
                    waterNow = waterNow,
                    waterBefore = waterBefore,
                    fish = fish,
                    // Кислород считает вода: он свойство водоёма, а не рыбы,
                    // и зависит от течения, ветра и того, была ли ночь.
                    oxygenMgL = ((water?.oxygenAt(hour.time) ?: oxygenSaturationMgL(waterNow)) +
                        place.oxygenOffsetMgL).coerceAtLeast(0.0)
                )
            } else {
                oxygenFactor(sorted, index, hour, fish)
            }
            val guild = Guild.of(fish.guild)
            val wind = windFactor(hour.windSpeed, guild)
            val light = lightFactor(hour.time, sunTimes, fish, guild)

            val factors = listOfNotNull(temperature, oxygen, pressure, trend, wind, light)

            // Ограничители перемножаются: непригодную для рыбы воду не
            // компенсирует ни давление, ни ветер. Условия клёва складываются
            // с весами и лишь масштабируют то, что осталось возможным.
            val placeBonus = place.bonusFor(guild)
            val habitat = factors.filter { it.limiting }.fold(1.0) { acc, f -> acc * f.value } *
                placeBonus
            val scored = factors.filterNot { it.limiting }
            // Веса нормируются по тем факторам, которые удалось посчитать:
            // без данных о восходе оценка не должна проседать на треть
            // просто потому, что фактор света отсутствует.
            val weights = scored.sumOf { it.weight }.takeIf { it > 0 } ?: 1.0
            val conditions = scored.sumOf { it.value * it.weight } / weights
            val score = (habitat * conditions * 100).roundToInt().coerceIn(0, 100)

            BiteForecast(
                time = hour.time,
                score = score,
                level = BiteLevel.fromScore(score),
                factors = factors + placeFactor(place, placeBonus)
            )
        }
    }

    /**
     * Температура — ограничитель: непригодную воду не искупает ничто.
     *
     * У вида два диапазона. В оптимуме рыба кормится, между оптимумом и
     * пределом выносливости активность тает, за пределом её нет. Ширина
     * этого перехода у каждого своя: карась терпит остывание до четырёх
     * градусов, а налим в двадцати уже задыхается, — поэтому и падение
     * считается от собственного предела вида, а не от общей константы.
     */
    private fun temperatureFactor(
        temperature: Double,
        fish: FishEntity,
        fromWater: Boolean
    ): BiteFactor {
        val optimum = fish.optMinTemp.toDouble()..fish.optMaxTemp.toDouble()
        val value = when {
            temperature in optimum -> 1.0
            temperature < optimum.start -> {
                val span = (optimum.start - fish.absMinTemp).coerceAtLeast(MIN_TOLERANCE)
                falloff(optimum.start - temperature, span)
            }
            else -> {
                val span = (fish.absMaxTemp - optimum.endInclusive).coerceAtLeast(MIN_TOLERANCE)
                falloff(temperature - optimum.endInclusive, span)
            }
        }

        val what = if (fromWater) "Вода" else "Воздух"
        return BiteFactor(
            name = if (fromWater) "Температура воды" else "Температура",
            value = value,
            weight = 0.0,
            limiting = true,
            comment = "$what ${temperature.roundToInt()}°C — " + when {
                temperature in optimum -> "оптимум вида"
                temperature < optimum.start && value > 0 -> "холоднее оптимума"
                temperature < optimum.start -> "холодно до оцепенения"
                value > 0 -> "теплее оптимума"
                else -> "жарко до оцепенения"
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
        val warmWaterStarts = fish.optMaxTemp.toDouble()
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
        fish: FishEntity,
        oxygenMgL: Double
    ): BiteFactor {
        val oxygen = oxygenMgL
        // Пороги берутся у вида: карасю хватает трёх миллиграммов, налиму
        // нужно шесть. Общая шкала уравняла бы их и соврала бы про обоих.
        val comfort = fish.oxygenComfortMgL.toDouble()
        val critical = fish.oxygenCriticalMgL.toDouble()
        val base = when {
            oxygen >= comfort -> 1.0
            oxygen <= critical -> 0.0
            else -> (oxygen - critical) / (comfort - critical).coerceAtLeast(MIN_TOLERANCE)
        }

        // Прогретая вода бьёт по рыбе раньше, чем растворимость дойдёт до
        // её порога: с теплом растёт и собственная потребность в кислороде.
        val warmWaterStarts = fish.optMaxTemp.toDouble()
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
            value = (minOf(base, warmthPenalty) + coolingBonus).coerceIn(0.0, 1.0),
            weight = 0.0,
            limiting = true,
            comment = "%.1f мг/л — %s".format(oxygen, oxygenLevelText(oxygenLevel(oxygen))) +
                when {
                    oxygen <= critical -> ", для этой рыбы критично"
                    cooling >= WATER_COOLING_STEP -> ", вода остывает"
                    cooling <= -WATER_COOLING_STEP -> ", вода прогревается"
                    waterNow > warmWaterStarts -> ", для этой рыбы вода тёплая"
                    else -> ""
                }
        )
    }

    /**
     * Место: слой и его структуры.
     *
     * Показывается отдельной строкой, даже когда ничего не меняет: рыболов
     * должен видеть, для какого места посчитан балл, иначе два разных числа
     * на одном экране выглядят ошибкой.
     */
    private fun placeFactor(place: PlaceContext, bonus: Double): BiteFactor = BiteFactor(
        name = "Место",
        value = bonus.coerceIn(0.0, 1.0),
        weight = 0.0,
        limiting = true,
        comment = place.title.replaceFirstChar { it.uppercase() } + when {
            place.structures.isEmpty() -> " — без особенностей"
            bonus > 1.05 -> ": " + place.structures.joinToString(", ") { it.name.lowercase() }
            bonus < 0.95 -> ": " + place.structures.joinToString(", ") { it.name.lowercase() } +
                " — это место против рыбы"
            else -> ": " + place.structures.joinToString(", ") { it.name.lowercase() }
        }
    )

    /**
     * Свет — тот самый ритм, по которому рыба живёт.
     *
     * Профиль берётся у вида, а если его нет — у гильдии. Без данных о
     * восходе фактор не участвует вовсе: выдумывать фазу по часам нельзя,
     * летний рассвет и зимний расходятся на пять часов.
     */
    private fun lightFactor(
        time: String,
        sunTimes: List<DailySunEntity>,
        fish: FishEntity,
        guild: Guild
    ): BiteFactor? {
        val moment = runCatching { LocalDateTime.parse(time) }.getOrNull() ?: return null
        val sun = sunTimes.firstOrNull { it.date == moment.toLocalDate().toString() }
        val phase = lightPhaseAt(moment, sun) ?: return null

        val value = lightActivity(phase, fish.lightActivity.decodeLightActivity(), guild)

        return BiteFactor(
            name = "Свет",
            value = value,
            weight = WEIGHT_LIGHT,
            comment = phase.title.replaceFirstChar { it.uppercase() } + " — " + when {
                value >= 0.9 -> "лучшее время этого вида"
                value >= 0.7 -> "рабочее время"
                value >= 0.4 -> "не лучший час"
                else -> "вид в это время стоит"
            }
        )
    }

    /**
     * Ветер.
     *
     * Зеркальная гладь — не идеал, а проблема: рябь ломает освещённость и
     * прячет рыболова, а заодно гонит корм к подветренному берегу. Поэтому у
     * ветра оптимум, а не монотонный штраф. Хищнику рябь важнее: он охотится
     * глазами и в прозрачной тихой воде сам виден издалека.
     */
    private fun windFactor(windSpeedKmh: Double, guild: Guild): BiteFactor {
        val ms = windSpeedKmh.kmhToMs()
        val rippleBonus = if (guild == Guild.PREDATOR) PREDATOR_RIPPLE else PEACEFUL_RIPPLE

        val value = when {
            ms < CALM_MS -> 1.0 - rippleBonus
            ms <= RIPPLE_MAX_MS -> 1.0
            ms <= STRONG_WIND_MS -> 0.7
            else -> 0.3
        }

        return BiteFactor(
            name = "Ветер",
            value = value,
            weight = WEIGHT_WIND,
            comment = "%.1f м/с".format(ms) + when {
                ms < CALM_MS -> " — штиль, вода как зеркало"
                ms <= RIPPLE_MAX_MS -> " — рябь на воде, это в плюс"
                ms <= STRONG_WIND_MS -> " — заметный ветер, вода перемешивается"
                else -> " — сильный ветер, рыбалка трудная"
            }
        )
    }

    /** Плавное затухание: у границы диапазона обрыва быть не должно. */
    private fun falloff(distance: Double, tolerance: Double): Double =
        (1.0 - distance / tolerance).coerceIn(0.0, 1.0)

    private companion object {
        // Веса условий клёва в сумме дают единицу: они распределяют то, что
        // осталось после ограничителей среды.
        //
        // Раньше 0.8 из этой единицы приходилось на давление и его
        // тенденцию — на параметр, влияние которого спорнее всего. Свет
        // бесспорен и при этом отличает виды друг от друга, поэтому часть
        // веса ушла ему.
        const val WEIGHT_PRESSURE = 0.35
        const val WEIGHT_TREND = 0.20
        const val WEIGHT_LIGHT = 0.30
        const val WEIGHT_WIND = 0.15

        /** Ширина перехода не бывает нулевой: иначе деление на ноль. */
        const val MIN_TOLERANCE = 1.0

        /** Отклонение от нормы водоёма, при котором активность падает до нуля. */
        const val PRESSURE_TOLERANCE = 12.0

        /** Отклонение, которое рыба ещё считает нормой. */
        const val AT_NORMAL_MMHG = 2.0

        const val TREND_WINDOW_HOURS = 3
        const val STABLE_PRESSURE_MMHG = 1.0
        const val NOTICEABLE_PRESSURE_MMHG = 3.0

        const val HEAT_TOLERANCE = 12.0
        const val NOTICEABLE_COOLING = 2.0

        /** Насколько должна сдвинуться вода за окно, чтобы это было ходом. */
        const val WATER_COOLING_STEP = 0.3
        const val COOLING_BONUS = 0.3

        /** Ниже этого ветра ряби нет: вода стоит зеркалом. */
        const val CALM_MS = 1.5

        /** Верх приятной ряби. */
        const val RIPPLE_MAX_MS = 5.0
        const val STRONG_WIND_MS = 9.0

        /** Насколько штиль хуже ряби: хищнику это важнее. */
        const val PREDATOR_RIPPLE = 0.25
        const val PEACEFUL_RIPPLE = 0.10
    }
}
