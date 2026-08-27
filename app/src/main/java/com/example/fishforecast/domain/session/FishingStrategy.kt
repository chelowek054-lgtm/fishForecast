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
    /** Где ловить: слой и почему. */
    val place: StrategyAdvice,
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

/** Что рыболов задал перед выездом. */
data class SessionPlanInput(
    val fish: FishEntity,
    val methodId: String?,
    val layer: WaterLayerChoice,
    val structureIds: List<String> = emptyList(),
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

    val water = when (input.layer) {
        WaterLayerChoice.SHALLOW -> conditions.waterShallowC
        WaterLayerChoice.DEEP -> conditions.waterDeepC
    }
    val cold = (water ?: Double.MAX_VALUE) < fish.coldTempThreshold

    val horizon = horizonAdvice(fish, guild, conditions, method)
    val warnings = warnings(fish, guild, conditions, method, horizon, input)

    return FishingStrategy(
        fish = fish,
        guild = guild,
        place = placeAdvice(input, conditions),
        horizon = horizon,
        bait = baitAdvice(fish, guild, cold, conditions, knowledge, backup = false),
        backupBait = baitAdvice(fish, guild, cold, conditions, knowledge, backup = true),
        groundbait = groundbaitAdvice(fish, guild, cold, conditions, input, method),
        window = windowAdvice(conditions),
        lookFor = fish.preferredStructures.decodeBaits().mapNotNull { knowledge.structure(it) },
        warnings = warnings
    )
}

private fun placeAdvice(
    input: SessionPlanInput,
    conditions: SessionConditions
): StrategyAdvice {
    val shallow = conditions.waterShallowC
    val deep = conditions.waterDeepC
    val reason = when {
        shallow == null || deep == null -> "Вода ещё не посчитана: обновите прогноз при сети"
        shallow > deep + 1 -> "Мель прогрета до %.0f°, в яме %.0f°".format(shallow, deep)
        deep > shallow + 1 -> "В яме теплее: %.0f° против %.0f° на мели".format(deep, shallow)
        else -> "Слои сравнялись: разницы между мелью и ямой сегодня нет"
    }

    return StrategyAdvice(
        title = "Место",
        value = input.layer.title.replaceFirstChar { it.uppercase() },
        reason = reason
    )
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
    val warmForFish = water != null && water > fish.optMaxTemp
    val poorOxygen = oxygen != null && oxygenLevel(oxygen) != OxygenLevel.RICH &&
        oxygen < fish.oxygenComfortMgL + 1

    val (value, reason) = when {
        warmForFish && poorOxygen -> "Толща воды" to
            "Вода %.0f° теплее оптимума и кислорода %.1f мг/л: у дна душно, рыба выше"
                .format(water, oxygen)

        warmForFish -> "Толща воды" to
            "Вода %.0f° теплее оптимума вида: у дна ей тяжелее, чем в полводы".format(water)

        conditions.lightPhase == LightPhase.DUSK && guild == Guild.PEACEFUL ->
            "Верх и полводы" to "Сумерки: мирная рыба поднимается к поверхности"

        else -> "Дно" to "Вода в пределах комфорта: рыба кормится со дна"
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
    horizon: StrategyAdvice,
    input: SessionPlanInput
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

    if (input.structureIds.isEmpty()) {
        add("Место без промера: пройдите точку грузилом, иначе корм ляжет вслепую")
    }

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
