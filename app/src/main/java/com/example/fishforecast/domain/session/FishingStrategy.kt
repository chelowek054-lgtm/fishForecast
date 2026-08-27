package com.example.fishforecast.domain.session

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.bite.BiteForecast
import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.fish.decodeBaits
import com.example.fishforecast.domain.fish.decodeGroundbait
import com.example.fishforecast.domain.knowledge.FishingMethod
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.domain.knowledge.LureGuide
import com.example.fishforecast.domain.knowledge.LureType
import com.example.fishforecast.domain.knowledge.StructureType
import com.example.fishforecast.domain.light.LightPhase
import com.example.fishforecast.domain.water.oxygenLevel
import com.example.fishforecast.domain.water.OxygenLevel
import kotlin.math.roundToInt

/**
 * План на выезд.
 *
 * Оценка клёва отвечает, стоит ли ехать. Но на воде этого мало: рыболов
 * стоит перед водой с конкретной снастью в руках, и вопрос у него другой —
 * на какой глубине, чем и сколько кормить.
 *
 * План собирается из того, что приложение уже считает: вода по слоям,
 * кислород, фаза света, тип водоёма. Каждый совет несёт причину — совет без
 * причины проверить нельзя, а рыбалка это ровно проверка.
 */
data class FishingStrategy(
    val fish: FishEntity,
    val guild: Guild,
    /** Где ловить: слой и почему. Приложение выбирает его само. */
    val place: StrategyAdvice,
    /** Куда рыба пойдёт в течение суток и что делать в каждый отрезок. */
    val day: List<DayPart> = emptyList(),
    /** На каком горизонте держать насадку. */
    val horizon: StrategyAdvice,
    /** Основная и запасная насадка либо приманка. */
    val bait: StrategyAdvice?,
    val backupBait: StrategyAdvice?,
    /** Прикормка; у хищника её нет. */
    val groundbait: StrategyAdvice?,
    /** Лучшее окно на ближайшие часы. */
    val window: StrategyAdvice?,
    /** Что проверить на месте: структуры, за которые цепляется этот вид. */
    val lookFor: List<StructureType>,
    /** Предупреждения: то, что может испортить выезд. */
    val warnings: List<String>
)

/** Совет с причиной: без неё нечего проверять. */
data class StrategyAdvice(
    val title: String,
    val value: String,
    val reason: String
)

/**
 * Отрезок суток с одинаковым советом.
 *
 * Рыба не стоит на месте: с рассветом выходит на мель кормиться, в полдень
 * уходит на глубину пережидать, к вечеру возвращается. Раскладка показывает
 * этот ход заранее — тогда рыболов приезжает не «на клёв вообще», а к
 * своему часу и на своё место.
 */
data class DayPart(
    val fromTime: String,
    val toTime: String,
    val phase: LightPhase?,
    val layer: WaterLayerChoice,
    val horizon: String,
    val score: Int,
    val waterC: Double?,
    val note: String
)

/** Час со всем, что о нём известно: по ним и строится раскладка суток. */
data class HourContext(
    val time: String,
    val phase: LightPhase?,
    val shallowC: Double?,
    val deepC: Double?,
    val oxygenMgL: Double?,
    val scoreShallow: Int,
    val scoreDeep: Int
)

/**
 * Что рыболов задал перед выездом.
 *
 * Про место его не спрашивают: куда пойдёт рыба, приложение считает само —
 * оно знает воду по слоям, кислород и ход света лучше, чем можно вспомнить
 * на берегу.
 */
data class SessionPlanInput(
    val fish: FishEntity,
    val methodId: String?,
    /** Есть ли с собой прикормка: без неё советы про закорм бессмысленны. */
    val hasGroundbait: Boolean = true
)

/** Условия, посчитанные приложением на ближайший час. */
data class SessionConditions(
    val hour: WeatherEntity?,
    val waterShallowC: Double?,
    val waterDeepC: Double?,
    val oxygenMgL: Double?,
    val lightPhase: LightPhase?,
    val forecast: List<BiteForecast> = emptyList(),
    /** Ближайшие сутки по часам: из них строится раскладка. */
    val hours: List<HourContext> = emptyList(),
    /** Осадки за прошедшие сутки, мм: по ним судим о мутности. */
    val rainLastDayMm: Double = 0.0
)

/**
 * Собирает план.
 *
 * Ничего не выдумывает: если данных нет, совет так и говорит. Пустой совет
 * честнее уверенного, но необоснованного.
 */
fun buildStrategy(
    input: SessionPlanInput,
    conditions: SessionConditions,
    knowledge: KnowledgeCatalog
): FishingStrategy {
    val fish = input.fish
    val guild = Guild.of(fish.guild)
    val method = knowledge.method(input.methodId)

    // Слой выбирается по нынешнему часу: там, где рыбе сейчас лучше.
    val layer = conditions.hours.firstOrNull()?.betterLayer() ?: WaterLayerChoice.SHALLOW
    val water = when (layer) {
        WaterLayerChoice.SHALLOW -> conditions.waterShallowC
        WaterLayerChoice.DEEP -> conditions.waterDeepC
    }
    val cold = (water ?: Double.MAX_VALUE) < fish.coldTempThreshold

    val horizon = horizonAdvice(fish, guild, conditions, method)
    val warnings = warnings(fish, guild, conditions, method, horizon)

    return FishingStrategy(
        fish = fish,
        guild = guild,
        place = placeAdvice(layer, conditions),
        day = dayParts(fish, guild, conditions),
        horizon = horizon,
        bait = baitAdvice(fish, guild, cold, conditions, knowledge, backup = false),
        backupBait = baitAdvice(fish, guild, cold, conditions, knowledge, backup = true),
        groundbait = groundbaitAdvice(fish, guild, cold, conditions, input, method),
        window = windowAdvice(conditions),
        lookFor = fish.preferredStructures.decodeBaits().mapNotNull { knowledge.structure(it) },
        warnings = warnings
    )
}

/**
 * Куда пойдёт рыба.
 *
 * Раньше это спрашивали у рыболова. Но выбор между мелью и ямой — не дело
 * вкуса: он следует из того, где рыбе сегодня легче дышать и кормиться, а
 * это приложение считает по обоим слоям.
 */
private fun placeAdvice(
    layer: WaterLayerChoice,
    conditions: SessionConditions
): StrategyAdvice {
    val shallow = conditions.waterShallowC
    val deep = conditions.waterDeepC
    val now = conditions.hours.firstOrNull()

    val reason = when {
        shallow == null || deep == null -> "Вода ещё не посчитана: обновите прогноз при сети"
        now != null && kotlin.math.abs(now.scoreShallow - now.scoreDeep) >= LAYER_DIFFERENCE ->
            if (layer == WaterLayerChoice.SHALLOW) {
                "На мели %.0f°, в яме %.0f°: у берега рыбе сейчас лучше (%d против %d)"
                    .format(shallow, deep, now.scoreShallow, now.scoreDeep)
            } else {
                "На мели %.0f°, в яме %.0f°: рыба ушла на глубину (%d против %d)"
                    .format(shallow, deep, now.scoreDeep, now.scoreShallow)
            }

        else -> "Мель %.0f°, яма %.0f° — слои почти сравнялись, решает не глубина, а укрытие"
            .format(shallow, deep)
    }

    return StrategyAdvice(
        title = "Куда идти",
        value = if (layer == WaterLayerChoice.SHALLOW) "Ближе к берегу, на мель" else "На глубину, в яму",
        reason = reason
    )
}

/**
 * Раскладка суток: где рыба будет и что делать в каждый отрезок.
 *
 * Соседние часы с одинаковым советом склеиваются — рыболову нужен ход дня,
 * а не двадцать четыре строки.
 */
private fun dayParts(
    fish: FishEntity,
    guild: Guild,
    conditions: SessionConditions
): List<DayPart> {
    if (conditions.hours.isEmpty()) return emptyList()

    val parts = mutableListOf<DayPart>()
    conditions.hours.take(HOURS_AHEAD).forEach { hour ->
        val layer = hour.betterLayer()
        val water = if (layer == WaterLayerChoice.SHALLOW) hour.shallowC else hour.deepC
        val horizon = horizonFor(fish, guild, water, hour.oxygenMgL, hour.phase)
        val score = maxOf(hour.scoreShallow, hour.scoreDeep)

        val last = parts.lastOrNull()
        if (last != null && last.layer == layer && last.horizon == horizon &&
            last.phase == hour.phase
        ) {
            parts[parts.lastIndex] = last.copy(
                toTime = hour.time.takeLast(5),
                score = maxOf(last.score, score)
            )
        } else {
            parts += DayPart(
                fromTime = hour.time.takeLast(5),
                toTime = hour.time.takeLast(5),
                phase = hour.phase,
                layer = layer,
                horizon = horizon,
                score = score,
                waterC = water,
                note = dayNote(guild, hour, layer, horizon)
            )
        }
    }
    return parts
}

private fun dayNote(
    guild: Guild,
    hour: HourContext,
    layer: WaterLayerChoice,
    horizon: String
): String = when {
    horizon != "Дно" -> "Рыба выше дна: донная снасть промолчит"
    layer == WaterLayerChoice.DEEP && hour.phase == LightPhase.DAY ->
        "Пережидает жару на глубине, кормится вяло"
    hour.phase == LightPhase.DAWN || hour.phase == LightPhase.DUSK ->
        if (guild == Guild.PREDATOR) "Зорька: хищник выходит на охоту" else "Зорька: выход на кормёжку"
    layer == WaterLayerChoice.SHALLOW -> "Выходит к берегу кормиться"
    else -> "Держится глубины"
}

/** Кому сегодня лучше: мели или яме. */
private fun HourContext.betterLayer(): WaterLayerChoice =
    if (scoreDeep - scoreShallow >= LAYER_DIFFERENCE) {
        WaterLayerChoice.DEEP
    } else {
        WaterLayerChoice.SHALLOW
    }

/**
 * Горизонт — то, чего приложению не хватало.
 *
 * В прогретой стоячей воде рыба поднимается над дном, и донная снасть в
 * этот момент бесполезна: насадка лежит там, где рыбы нет. Поэтому горизонт
 * считается, а не берётся из справочника как свойство вида.
 */
private fun horizonAdvice(
    fish: FishEntity,
    guild: Guild,
    conditions: SessionConditions,
    method: FishingMethod?
): StrategyAdvice {
    val water = conditions.waterShallowC
    val oxygen = conditions.oxygenMgL
    val value = horizonFor(fish, guild, water, oxygen, conditions.lightPhase)
    val warmForFish = water != null && water > fish.optMaxTemp
    val poorOxygen = oxygen != null && oxygenLevel(oxygen) != OxygenLevel.RICH &&
        oxygen < fish.oxygenComfortMgL + 1

    val reason = when {
        warmForFish && poorOxygen ->
            "Вода %.0f° теплее оптимума и кислорода %.1f мг/л: у дна душно, рыба выше"
                .format(water, oxygen)

        warmForFish -> "Вода %.0f° теплее оптимума вида: у дна ей тяжелее, чем в полводы"
            .format(water)

        value != "Дно" -> "Сумерки: мирная рыба поднимается к поверхности"
        else -> "Вода в пределах комфорта: рыба кормится со дна"
    }

    val mismatch = method != null && method.horizon == "bottom" && value != "Дно"
    return StrategyAdvice(
        title = "Горизонт",
        value = value,
        reason = if (mismatch) {
            "$reason. «${method!!.name}» работает по дну — нужна снасть для толщи"
        } else {
            reason
        }
    )
}

/**
 * На каком горизонте держать насадку в этот час.
 *
 * В прогретой воде рыба поднимается над дном, и донная снасть в этот момент
 * бесполезна: насадка лежит там, где рыбы нет.
 */
private fun horizonFor(
    fish: FishEntity,
    guild: Guild,
    water: Double?,
    oxygen: Double?,
    phase: LightPhase?
): String {
    val warmForFish = water != null && water > fish.optMaxTemp
    val poorOxygen = oxygen != null && oxygenLevel(oxygen) != OxygenLevel.RICH &&
        oxygen < fish.oxygenComfortMgL + 1

    return when {
        warmForFish -> "Толща воды"
        phase == LightPhase.DUSK && guild == Guild.PEACEFUL -> "Верх и полводы"
        poorOxygen && water != null && water > fish.optMaxTemp - 2 -> "Толща воды"
        else -> "Дно"
    }
}

private fun baitAdvice(
    fish: FishEntity,
    guild: Guild,
    cold: Boolean,
    conditions: SessionConditions,
    knowledge: KnowledgeCatalog,
    backup: Boolean
): StrategyAdvice? {
    if (guild == Guild.PREDATOR) return lureAdvice(conditions, knowledge, backup)

    val baits = (if (cold) fish.baitsCold else fish.baitsWarm).decodeBaits()
    val choice = baits.getOrNull(if (backup) 1 else 0) ?: return null

    return StrategyAdvice(
        title = if (backup) "Запасная насадка" else "Насадка",
        value = choice,
        reason = if (cold) {
            "Вода холоднее ${fish.coldTempThreshold.roundToInt()}°: рыба берёт животное"
        } else {
            "Вода теплее ${fish.coldTempThreshold.roundToInt()}°: работает растительное и сладкое"
        }
    )
}

/**
 * Приманка хищника: тип, цвет, размер и подача.
 *
 * Прозрачность оценивается по осадкам за сутки: ливень поднимает муть, и
 * тогда рыба ищет приманку боковой линией, а не глазами.
 */
private fun lureAdvice(
    conditions: SessionConditions,
    knowledge: KnowledgeCatalog,
    backup: Boolean
): StrategyAdvice? {
    val clarity = if (conditions.rainLastDayMm >= MUDDY_RAIN_MM) "stained" else "clear"
    val light = when (conditions.lightPhase) {
        LightPhase.NIGHT -> "dark"
        LightPhase.DAWN, LightPhase.DUSK, LightPhase.EVENING -> "low"
        else -> "bright"
    }
    val coldWater = (conditions.waterShallowC ?: 20.0) < COLD_WATER_C

    // Правило про температуру старше общего: в холодной воде важнее не
    // цвет, а то, что приманку надо вести медленно.
    val guide = knowledge.lureGuides.firstOrNull { rule ->
        rule.water != null && (rule.water == "cold") == coldWater && rule.clarity == clarity
    }
        ?: knowledge.lureGuides.firstOrNull { rule ->
            rule.water == null && rule.clarity == clarity && rule.light == light
        }
        ?: knowledge.lureGuides.firstOrNull { it.clarity == clarity }
        ?: return null

    val type = lureTypeFor(conditions, knowledge, coldWater, backup) ?: return null
    val color = guide.colors.getOrNull(if (backup) 1 else 0) ?: guide.colors.firstOrNull().orEmpty()

    return StrategyAdvice(
        title = if (backup) "Запасная приманка" else "Приманка",
        value = listOf(type.name, color, guide.size).filter { it.isNotBlank() }.joinToString(", "),
        reason = listOf(guide.notes, "Подача: ${guide.action}")
            .filter { it.isNotBlank() }
            .joinToString(" ")
    )
}

private fun lureTypeFor(
    conditions: SessionConditions,
    knowledge: KnowledgeCatalog,
    coldWater: Boolean,
    backup: Boolean
): LureType? {
    val order = when {
        coldWater -> listOf("soft_jig", "spoon", "crank")
        conditions.lightPhase == LightPhase.DUSK || conditions.lightPhase == LightPhase.DAWN ->
            listOf("crank", "popper", "spinner")

        else -> listOf("soft_jig", "spinner", "crank")
    }
    val ids = if (backup) order.drop(1) else order
    return ids.firstNotNullOfOrNull { knowledge.lureType(it) }
}

/**
 * Прикормка.
 *
 * Справочник задаёт состав от температуры, но объём должен зависеть ещё и
 * от того, готова ли рыба есть. В прогретой воде с бедным кислородом
 * обильный стол собирает мелочь и поднимает рыбу над кормом, а не сажает
 * её на точку.
 */
private fun groundbaitAdvice(
    fish: FishEntity,
    guild: Guild,
    cold: Boolean,
    conditions: SessionConditions,
    input: SessionPlanInput,
    method: FishingMethod?
): StrategyAdvice? {
    if (guild == Guild.PREDATOR) {
        return StrategyAdvice(
            title = "Прикормка",
            value = "Не нужна",
            reason = "Хищника собирают приманкой, а не столом"
        )
    }
    if (!input.hasGroundbait || method?.groundbait == false) return null

    val rule = (if (cold) fish.groundbaitCold else fish.groundbaitWarm).decodeGroundbait()
    val oxygen = conditions.oxygenMgL
    val water = conditions.waterShallowC
    val heatCut = water != null && water > fish.optMaxTemp - HEAT_MARGIN_C &&
        oxygen != null && oxygen < fish.oxygenComfortMgL + 1

    val volume = if (heatCut) "меньше обычного" else volumeWord(rule.volume)
    val reason = if (heatCut) {
        "Вода %.0f° и кислорода %.1f мг/л: обильный стол сейчас во вред — рыба встанет над кормом"
            .format(water, oxygen)
    } else {
        rule.notes.ifBlank { "По справочнику для этой воды" }
    }

    return StrategyAdvice(
        title = "Прикормка",
        value = listOf(volume, fractionWord(rule.fraction), sweetWord(rule.sweetness))
            .filter { it.isNotBlank() }
            .joinToString(", "),
        reason = reason
    )
}

private fun windowAdvice(conditions: SessionConditions): StrategyAdvice? {
    val best = conditions.forecast.maxByOrNull { it.score } ?: return null
    val now = conditions.forecast.firstOrNull()?.score ?: return null

    return StrategyAdvice(
        title = "Окно",
        value = best.time.takeLast(5),
        reason = if (best.score > now + WINDOW_DIFFERENCE) {
            "Сейчас $now, в это время ${best.score}: лучший час впереди"
        } else {
            "Ровный ход: ждать особого выхода не приходится"
        }
    )
}

private fun warnings(
    fish: FishEntity,
    guild: Guild,
    conditions: SessionConditions,
    method: FishingMethod?,
    horizon: StrategyAdvice
): List<String> = buildList {
    val oxygen = conditions.oxygenMgL
    if (oxygen != null && oxygen < fish.oxygenComfortMgL) {
        add(
            "Кислорода %.1f мг/л при норме вида %.1f: рыба бережёт силы, поклёвки будут вялыми"
                .format(oxygen, fish.oxygenComfortMgL)
        )
    }

    if (method != null && method.horizon == "bottom" && horizon.value != "Дно") {
        add("Снасть работает по дну, а рыба сегодня выше: возьмите с собой снасть для толщи")
    }

    add("Точку стоит пройти грузилом: без промера корм ложится вслепую")

    val water = conditions.waterShallowC
    if (water != null && water > fish.absMaxTemp) {
        add("Вода теплее предела вида: сегодня стоит ехать за кем-то другим")
    }

    if (guild == Guild.PEACEFUL && conditions.lightPhase == LightPhase.DAY) {
        add("Полдень: у мирной рыбы это худший час, лучше дождаться вечера")
    }
}

private fun volumeWord(value: String): String = when (value) {
    "none" -> "не кормить"
    "low" -> "мало корма"
    "medium" -> "умеренно"
    "high" -> "обильно"
    else -> value
}

private fun fractionWord(value: String): String = when (value) {
    "none" -> ""
    "ultra_fine" -> "пылящая"
    "fine" -> "мелкая фракция"
    "fine_medium" -> "мелкая и средняя"
    "medium" -> "средняя фракция"
    "coarse" -> "крупная фракция"
    else -> value
}

private fun sweetWord(value: String): String = when (value) {
    "none" -> "без сладости"
    "low" -> "чуть сладкая"
    "medium" -> "умеренно сладкая"
    "high" -> "сладкая"
    else -> value
}

/** Столько дождя за сутки уже поднимает муть. */
private const val MUDDY_RAIN_MM = 8.0

/** Ниже этой воды хищник не догоняет быструю приманку. */
private const val COLD_WATER_C = 12.0

/** За сколько градусов до предела вида стол пора урезать. */
private const val HEAT_MARGIN_C = 2.0

/** Насколько лучший час должен опережать нынешний, чтобы его ждать. */
private const val WINDOW_DIFFERENCE = 10

/** Разрыв между слоями, ниже которого выбирать глубину незачем. */
private const val LAYER_DIFFERENCE = 5

/** На сколько часов вперёд расписывается ход суток. */
private const val HOURS_AHEAD = 24
