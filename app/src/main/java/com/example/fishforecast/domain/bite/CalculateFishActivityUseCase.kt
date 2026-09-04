package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.fish.decodeLightActivity
import com.example.fishforecast.domain.light.lightActivity
import com.example.fishforecast.domain.light.lightPhaseAt
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.domain.weather.isNortherlyWind
import com.example.fishforecast.domain.weather.kmhToMs
import com.example.fishforecast.domain.weather.windDirectionLabel
import com.example.fishforecast.domain.weather.windTurn
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
        place: PlaceContext = PlaceContext(),
        observations: List<ActiveObservation> = emptyList()
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
            // Своё окно: вода инертна, и за три часа её ход почти не читается.
            val waterTrendBefore = water?.let { state ->
                sorted.getOrNull(index - WATER_TREND_HOURS)
                    ?.let { state.layerAt(it.time, place.layer) }
                    ?.plus(place.waterOffsetC)
            }

            val temperature = temperatureFactor(waterNow ?: hour.temperature, fish, waterNow != null)
            val pressure = pressureFactor(hour.pressure.hPaToMmHg(), normal, fish)
            val trend = pressureTrendFactor(sorted, index, normal, fish)
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
            val wind = windFactor(sorted, index, guild, waterNow, fish)
            val light = lightFactor(hour.time, sunTimes, fish, guild)
            // Ход суток и ход воды отвечают на вопрос «откуда пришли»:
            // одна и та же погода читается по-разному в зависимости от того,
            // росло давление вторые сутки или падало перед фронтом.
            val dayTrend = pressureDayFactor(sorted, index, normal)
            val waterTrend = waterTrendFactor(waterNow, waterTrendBefore, fish, WATER_TREND_HOURS)
            val stratification = stratificationFactor(sorted, index, place, waterNow, fish)
            // Увиденное своими глазами весомее расчёта, но живёт недолго:
            // поправка тает к концу срока, записанного в словаре.
            val noticed = observationFactor(observations, guild, hour.time)

            val factors = listOfNotNull(
                temperature, oxygen, pressure, trend, dayTrend,
                waterTrend, wind, light, stratification, noticed
            )

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

    /**
     * Отклонение от нормы водоёма важнее абсолютного значения.
     *
     * Допуск берётся у вида и несимметричен: падение рыба переносит легче
     * роста — падающее давление она встречает кормлением, растущее вгоняет в
     * апатию. Абсолютных границ у вида больше нет: на высоте 500 м норма
     * около 710 мм, и жёсткие 740–755 объявили бы обычный для места фон
     * катастрофой.
     */
    private fun pressureFactor(
        pressureMmHg: Double,
        normal: Double?,
        fish: FishEntity
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
        val tolerance = if (pressureMmHg < normal) {
            fish.maxPressureDrop.toDouble()
        } else {
            fish.maxPressureRise.toDouble()
        }.coerceAtLeast(MIN_TOLERANCE)
        val value = falloff(deviation, tolerance)

        return BiteFactor(
            name = "Давление",
            value = value,
            weight = WEIGHT_PRESSURE,
            comment = "${pressureMmHg.roundToInt()} мм рт. ст. " + when {
                deviation <= AT_NORMAL_MMHG -> "— норма водоёма"
                deviation >= tolerance && pressureMmHg < normal ->
                    "— ниже нормы на ${deviation.roundToInt()}, вид столько не терпит"
                deviation >= tolerance ->
                    "— выше нормы на ${deviation.roundToInt()}, вид столько не терпит"
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
        normal: Double?,
        fish: FishEntity
    ): BiteFactor {
        // Окно у каждого вида своё: карп отыгрывает перепад за пару часов,
        // судака тот же перепад держит половину суток. Общее трёхчасовое окно
        // для первого было слишком длинным, для второго — слишком коротким.
        val window = fish.pressureRecoveryHours.coerceIn(MIN_RECOVERY_HOURS, MAX_RECOVERY_HOURS)
        // Медленной рыбе тенденция важнее: она из перепада дольше выбирается.
        val weight = WEIGHT_TREND * (RECOVERY_WEIGHT_BASE + RECOVERY_WEIGHT_STEP * window)
            .coerceIn(RECOVERY_WEIGHT_MIN, RECOVERY_WEIGHT_MAX)
        val previousIndex = index - window
        if (previousIndex < 0) {
            return BiteFactor(
                name = "Тенденция",
                value = 1.0,
                weight = weight,
                comment = "Недостаточно истории за $window ч"
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
            weight = weight,
            comment = when {
                change <= STABLE_PRESSURE_MMHG -> "Давление стабильно за $window ч"
                normal == null -> "Меняется на ${change.roundToInt()} мм за $window ч"
                movingToNormal -> "Возвращается к норме (${change.roundToInt()} мм за $window ч)"
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

        // Остывание добавляет кислорода, дальнейший прогрев — отнимает. Но
        // только в тепле: в холодной воде кислорода и так вдоволь.
        val oxygenAtStake = hour.temperature > warmWaterStarts - HEAT_MARGIN_C
        val coolingBonus = when {
            !oxygenAtStake -> 0.0
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

        // Ход воды правит кислород только в тепле. В пятиградусной воде его
        // почти тринадцать миллиграммов на литр — прогрев на полградуса там
        // ничего не отнимает, и штрафовать за него значит выдавать весну за
        // ухудшение. За сам ход отвечает отдельный фактор.
        val oxygenAtStake = waterNow > warmWaterStarts - HEAT_MARGIN_C
        val cooling = waterBefore?.let { it - waterNow } ?: 0.0
        val coolingBonus = when {
            !oxygenAtStake -> 0.0
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
     * Ход давления за сутки.
     *
     * Трёхчасовое окно ловит момент, но не рассказывает, откуда пришли.
     * Между тем рыба отзывается именно на затяжное движение: сутки падения
     * перед фронтом — это жор, сутки роста после фронта — вялый клёв, даже
     * когда в последние три часа всё замерло.
     *
     * Оговорка, которую стоит помнить: прямой связи давления с кормлением
     * наука не показала, потому что отделить его от прочей погоды в поле не
     * удаётся. Здесь это эвристика, проверенная практикой, — и потому она
     * весит меньше, чем отклонение от нормы места.
     */
    private fun pressureDayFactor(
        forecast: List<WeatherEntity>,
        index: Int,
        normal: Double?
    ): BiteFactor {
        val previousIndex = (index - DAY_WINDOW_HOURS).coerceAtLeast(0)
        val hoursBack = index - previousIndex
        if (hoursBack < MIN_DAY_WINDOW_HOURS) {
            return BiteFactor(
                name = "Ход за сутки",
                value = 1.0,
                weight = WEIGHT_PRESSURE_DAY,
                comment = "Истории меньше $MIN_DAY_WINDOW_HOURS часов — сутки не прочитать"
            )
        }

        val current = forecast[index].pressure.hPaToMmHg()
        val previous = forecast[previousIndex].pressure.hPaToMmHg()
        val change = current - previous
        val atNormal = normal != null && abs(current - normal) <= AT_NORMAL_MMHG

        val value = when {
            change <= -DAY_FALL_MMHG -> 1.0
            change <= -STABLE_PRESSURE_MMHG -> 0.9
            change < STABLE_PRESSURE_MMHG -> if (atNormal) 0.9 else 0.8
            change >= DAY_RISE_MMHG -> 0.35
            // Рост у самой нормы места рыба переносит легче: фон привычный,
            // меняется только его сторона.
            atNormal -> 0.65
            else -> 0.5
        }

        return BiteFactor(
            name = "Ход за сутки",
            value = value,
            weight = WEIGHT_PRESSURE_DAY,
            comment = "%+.0f мм за %d ч".format(change, hoursBack) + when {
                change <= -DAY_FALL_MMHG -> " — падает перед фронтом, рыба кормится впрок"
                change <= -STABLE_PRESSURE_MMHG -> " — понемногу падает, это в плюс"
                change < STABLE_PRESSURE_MMHG -> " — сутки ровные"
                change >= DAY_RISE_MMHG -> " — резкий рост после фронта, рыба прижата"
                atNormal -> " — растёт, но уже у нормы места"
                else -> " — растёт после фронта, клёв вялый"
            }
        )
    }

    /**
     * Куда идёт вода относительно оптимума вида.
     *
     * Раньше прогрев всегда шёл в минус — через кислород. Для перегретой воды
     * это верно, а для холодной прямо наоборот: несколько тёплых дней весной
     * поднимают воду на пару градусов, и рыба выходит кормиться. Знак у хода
     * воды зависит не от направления, а от того, с какой стороны от оптимума
     * она сейчас стоит.
     */
    private fun waterTrendFactor(
        waterNow: Double?,
        waterBefore: Double?,
        fish: FishEntity,
        hours: Int
    ): BiteFactor? {
        if (waterNow == null || waterBefore == null) return null

        val delta = waterNow - waterBefore
        val warming = delta >= WATER_TREND_STEP
        val cooling = delta <= -WATER_TREND_STEP
        val belowOptimum = waterNow < fish.optMinTemp
        val aboveOptimum = waterNow > fish.optMaxTemp

        val value = when {
            belowOptimum && warming -> 1.0
            belowOptimum && cooling -> 0.55
            belowOptimum -> 0.8
            aboveOptimum && cooling -> 1.0
            aboveOptimum && warming -> 0.5
            aboveOptimum -> 0.75
            warming || cooling -> 0.9
            else -> 1.0
        }

        return BiteFactor(
            name = "Ход воды",
            value = value,
            weight = WEIGHT_WATER_TREND,
            comment = "%+.1f° за %d ч".format(delta, hours) + when {
                belowOptimum && warming -> " — холодная вода греется, рыба выходит кормиться"
                belowOptimum && cooling -> " — холодает, рыба замирает"
                belowOptimum -> " — холодная вода стоит на месте"
                aboveOptimum && cooling -> " — жара отпускает, рыба оживает"
                aboveOptimum && warming -> " — перегретая вода греется дальше"
                aboveOptimum -> " — жарко и без перемен"
                else -> " — вода в оптимуме вида"
            }
        )
    }

    /**
     * Ветер: сила, постоянство и сторона.
     *
     * Зеркальная гладь — не идеал, а проблема: рябь ломает освещённость и
     * прячет рыболова. Но одной скорости мало. Ветер, который сутки дует в
     * один берег, сгоняет туда планктон, за ним малька, за мальком хищника —
     * это и есть наветренный берег, ради которого встают против ветра.
     * Развернувшийся ветер означает обратное: прошёл фронт, корм понесло в
     * другую сторону, и рыбе надо заново искать стол.
     *
     * Северный ветер приносит холодный воздух. В холодной воде это против
     * рыболова, в перегретой — за него: верхний слой остывает и берёт
     * кислород.
     */
    private fun windFactor(
        forecast: List<WeatherEntity>,
        index: Int,
        guild: Guild,
        waterNow: Double?,
        fish: FishEntity
    ): BiteFactor {
        val hour = forecast[index]
        val ms = hour.windSpeed.kmhToMs()
        val rippleBonus = if (guild == Guild.PREDATOR) PREDATOR_RIPPLE else PEACEFUL_RIPPLE

        val base = when {
            ms < CALM_MS -> 1.0 - rippleBonus
            ms <= RIPPLE_MAX_MS -> 1.0
            ms <= STRONG_WIND_MS -> 0.7
            else -> 0.3
        }

        val before = forecast.getOrNull(index - WIND_SHIFT_HOURS)
        val turn = before?.let { windTurn(it.windDirection, hour.windDirection) }
        // Штиль не «разворачивается»: направление у слабого ветра случайно.
        val turned = turn != null && turn >= WIND_TURN_DEG && ms >= CALM_MS
        val steady = turn != null && turn <= WIND_STEADY_DEG &&
            ms >= CALM_MS && ms <= STRONG_WIND_MS
        val northerly = ms >= CALM_MS && isNortherlyWind(hour.windDirection)
        val hotWater = waterNow != null && waterNow > fish.optMaxTemp
        val coldWater = waterNow != null && waterNow < fish.optMinTemp

        var value = base
        if (turned) value *= WIND_TURN_PENALTY
        if (steady) value *= WIND_STEADY_BONUS
        if (northerly && coldWater) value *= NORTH_COLD_PENALTY
        if (northerly && hotWater) value *= NORTH_HEAT_BONUS

        return BiteFactor(
            name = "Ветер",
            value = value.coerceIn(0.0, 1.0),
            weight = WEIGHT_WIND,
            comment = "%s %.1f м/с".format(windDirectionLabel(hour.windDirection), ms) + when {
                turned -> " — развернулся за $WIND_SHIFT_HOURS ч, рыба перестраивается"
                northerly && coldWater -> " — северный, студит и без того холодную воду"
                northerly && hotWater -> " — северный, сбивает жару, кислорода прибавится"
                steady && ms > CALM_MS -> " — держит сторону, корм идёт к наветренному берегу"
                ms < CALM_MS -> " — штиль, вода как зеркало"
                ms <= RIPPLE_MAX_MS -> " — рябь на воде, это в плюс"
                ms <= STRONG_WIND_MS -> " — заметный ветер, вода перемешивается"
                else -> " — сильный ветер, рыбалка трудная"
            }
        )
    }

    /**
     * Расслоение воды в штиль.
     *
     * Перемешивает водоём ветер. Когда его нет несколько часов подряд, а
     * вода перегрета, столб распадается на слои: тёплый верх, холодный низ и
     * термоклин между ними. Рыба уходит из верхнего слоя, но и в яму не
     * идёт — там нечем дышать, — а стоит в термоклине и кормится вяло.
     *
     * Фактор появляется только тогда, когда всё это сошлось: без штиля или
     * без жары его в списке нет, и лишней строки на экране не возникает.
     */
    private fun stratificationFactor(
        forecast: List<WeatherEntity>,
        index: Int,
        place: PlaceContext,
        waterNow: Double?,
        fish: FishEntity
    ): BiteFactor? {
        if (waterNow == null || waterNow <= fish.optMaxTemp) return null

        val from = index - CALM_SPELL_HOURS + 1
        if (from < 0) return null
        val calm = (from..index).all { forecast[it].windSpeed.kmhToMs() < CALM_MS }
        if (!calm) return null

        val deep = place.layer == WaterLayerChoice.DEEP
        return BiteFactor(
            name = "Расслоение",
            value = if (deep) STRATIFIED_DEEP else STRATIFIED_SHALLOW,
            weight = 0.0,
            limiting = true,
            comment = "Штиль $CALM_SPELL_HOURS ч на перегретой воде: она расслоилась, " +
                if (deep) "рыба держится термоклина над ямой" else "рыба ушла с мели вниз"
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

        // Веса динамики. Сумма всех весов больше единицы — так и задумано:
        // расчёт нормирует их по тем факторам, которые удалось посчитать, и
        // на устройстве без истории оценка не проседает просто от нехватки
        // данных. Действующие доли: давление 0.29, тенденция 0.17, ход за
        // сутки 0.13, свет 0.25, ветер 0.13, ход воды 0.04.
        const val WEIGHT_PRESSURE_DAY = 0.15
        const val WEIGHT_WATER_TREND = 0.05

        /** Ширина перехода не бывает нулевой: иначе деление на ноль. */
        const val MIN_TOLERANCE = 1.0

        /** Отклонение, которое рыба ещё считает нормой. */
        const val AT_NORMAL_MMHG = 2.0

        /** Окно хода воды и кислорода: оно про воду, а не про пузырь рыбы. */
        const val TREND_WINDOW_HOURS = 3

        /** Границы окна тенденции: чужой справочник вправе прислать что угодно. */
        const val MIN_RECOVERY_HOURS = 1
        const val MAX_RECOVERY_HOURS = 24

        /**
         * Вес тенденции растёт с временем отыгрыша: рыба, которая выбирается
         * из перепада половину суток, зависит от него сильнее, чем та, что
         * приходит в себя за два часа.
         */
        const val RECOVERY_WEIGHT_BASE = 0.7
        const val RECOVERY_WEIGHT_STEP = 0.05
        const val RECOVERY_WEIGHT_MIN = 0.7
        const val RECOVERY_WEIGHT_MAX = 1.6
        const val STABLE_PRESSURE_MMHG = 1.0
        const val NOTICEABLE_PRESSURE_MMHG = 3.0

        /** Окно суточного хода и минимум истории, при котором его читают. */
        const val DAY_WINDOW_HOURS = 24
        const val MIN_DAY_WINDOW_HOURS = 6

        /** Падение за сутки, которое рыба встречает жором. */
        const val DAY_FALL_MMHG = 2.5

        /** Рост за сутки, после которого клёв замирает. */
        const val DAY_RISE_MMHG = 4.0

        /** Окно хода воды: за три часа её движение почти не читается. */
        const val WATER_TREND_HOURS = 6

        /** Насколько должна сдвинуться вода за окно, чтобы это был ход. */
        const val WATER_TREND_STEP = 0.3

        /** За сколько часов сравнивается направление ветра. */
        const val WIND_SHIFT_HOURS = 6

        /** Поворот, после которого ветер считается сменившимся. */
        const val WIND_TURN_DEG = 90.0

        /** Разброс, в пределах которого ветер считается устойчивым. */
        const val WIND_STEADY_DEG = 45.0

        const val WIND_TURN_PENALTY = 0.8
        const val WIND_STEADY_BONUS = 1.1
        const val NORTH_COLD_PENALTY = 0.85
        const val NORTH_HEAT_BONUS = 1.1

        /** Сколько часов штиля расслаивают перегретую воду. */
        const val CALM_SPELL_HOURS = 6

        /** Что остаётся от шанса на мели и в яме, когда вода расслоилась. */
        const val STRATIFIED_SHALLOW = 0.8
        const val STRATIFIED_DEEP = 0.95

        const val HEAT_TOLERANCE = 12.0

        /** За сколько градусов до верха оптимума кислород становится узким местом. */
        const val HEAT_MARGIN_C = 2.0
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
