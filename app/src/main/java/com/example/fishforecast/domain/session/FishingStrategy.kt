package com.example.fishforecast.domain.session

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.bite.BiteForecast
import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.fish.decodeBaits
import com.example.fishforecast.domain.fish.decodeGroundbait
import com.example.fishforecast.domain.knowledge.BaitingPlan
import com.example.fishforecast.domain.knowledge.FishingMethod
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.domain.knowledge.LureGuide
import com.example.fishforecast.domain.knowledge.LureType
import com.example.fishforecast.domain.knowledge.StructureType
import com.example.fishforecast.domain.fish.flavorText
import com.example.fishforecast.domain.fish.horizonText
import com.example.fishforecast.domain.fish.horizonTitle
import com.example.fishforecast.domain.light.LightPhase
import com.example.fishforecast.domain.season.SeasonPhase
import com.example.fishforecast.domain.season.SeasonState
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
    /**
     * Чем вид занят в это время года.
     *
     * Стоит первым: пока не сказано, что рыба на нересте или спит, все
     * остальные советы читаются как обещание, что она вообще кормится.
     */
    val season: StrategyAdvice?,
    /**
     * Насколько рыба готова есть: 0 — не ест вовсе, 1 — берёт всё.
     *
     * Из него следует объём стола, размер насадки и темп докорма. Раньше эти
     * три вещи решались порогом «холодно или тепло», хотя аппетит меняется
     * плавно и зависит ещё и от сезона.
     */
    val appetite: Double,
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
    /** Как рассыпать корм: ковром, точкой, дорожкой или программой. */
    val baiting: StrategyAdvice?,
    /** Размер и твёрдость насадки: ими отсекают мелочь. */
    val selection: StrategyAdvice?,
    /** Монтаж под выбранный способ. */
    val rig: StrategyAdvice?,
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
    val hasGroundbait: Boolean = true,
    /** За кем едут: от этого зависит и стол, и размер насадки. */
    val goal: CatchGoal = CatchGoal.NUMBERS
)

/**
 * За кем едут.
 *
 * Вопрос не про амбиции, а про снасть. Мелкая и средняя рыба ходит стаей и
 * кормится наперегонки: её зовут ковром мелкой фракции и берут на мелкую
 * насадку. Крупная держится одиночкой, идёт своим маршрутом и осматривает
 * точку прежде, чем сесть за неё: ей нужен корм, к которому она привыкла, и
 * насадка, которую молодняк не утащит.
 *
 * Одной снастью два ответа сразу не получить — поэтому вопрос задаётся, а
 * не угадывается.
 */
enum class CatchGoal(val title: String) {
    NUMBERS("За количеством"),
    TROPHY("За трофеем");

    /** Слово словаря: справочник ходит между устройствами, перевод — дело экрана. */
    val key: String get() = if (this == TROPHY) "trophy" else "numbers"
}

/** Условия, посчитанные приложением на ближайший час. */
data class SessionConditions(
    val hour: WeatherEntity?,
    val waterShallowC: Double?,
    val waterDeepC: Double?,
    val oxygenMgL: Double?,
    val lightPhase: LightPhase?,
    val forecast: List<BiteForecast> = emptyList(),
    /** Тип водоёма района: на большой воде и на пруду кормят по-разному. */
    val waterBodyId: String? = null,
    /** Ближайшие сутки по часам: из них строится раскладка. */
    val hours: List<HourContext> = emptyList(),
    /** Осадки за прошедшие сутки, мм: по ним судим о мутности. */
    val rainLastDayMm: Double = 0.0,
    /** Фаза сезона вида; null — истории воды не хватило, чтобы её определить. */
    val season: SeasonState? = null
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
    val season = conditions.season
    val appetite = appetiteOf(fish, water, season, conditions)

    val horizon = horizonAdvice(fish, guild, conditions, method)
    val warnings = warnings(fish, guild, conditions, method, horizon)

    // Схема закорма и размер насадки — один выбор: корм и насадка должны
    // звать одну и ту же рыбу, иначе стол собирает молодняк, а крючок ждёт
    // трофея.
    val largeWater = knowledge.waterBody(conditions.waterBodyId)?.large ?: false
    val baitingPlan = if (guild == Guild.PREDATOR) {
        null
    } else {
        baitingPlanFor(input.goal, cold, largeWater, knowledge)
    }

    return FishingStrategy(
        fish = fish,
        guild = guild,
        season = seasonAdvice(fish, season),
        appetite = appetite,
        place = placeAdvice(layer, conditions),
        day = dayParts(fish, guild, conditions),
        horizon = horizon,
        bait = baitAdvice(fish, guild, cold, season, conditions, knowledge, backup = false),
        backupBait = baitAdvice(fish, guild, cold, season, conditions, knowledge, backup = true),
        groundbait = groundbaitAdvice(fish, guild, cold, appetite, conditions, input, method),
        baiting = baitingAdvice(baitingPlan, input, cold, appetite, method),
        selection = selectionAdvice(baitingPlan, appetite),
        rig = rigAdvice(method),
        window = windowAdvice(conditions),
        lookFor = fish.preferredStructures.decodeBaits().mapNotNull { knowledge.structure(it) },
        warnings = warnings
    )
}

/**
 * Насколько рыба готова есть: 0…1.
 *
 * Три вещи складываются в одно число. Первое — насколько вода далека от
 * оптимума вида: опорожнение кишечника у карповых зависит от температуры
 * экспонентой, поэтому аппетит и падает плавно, а не ступенькой у порога.
 * Второе — фаза сезона: нерестящаяся рыба не ест при любой воде, а идущая к
 * нересту ест сверх обычного. Третье — сегодняшняя погода в виде балла клёва:
 * при давлении, которое вид не терпит, кормить полным столом незачем.
 *
 * Отсюда объём корма, размер насадки и темп докорма — то, что раньше решалось
 * одним порогом «холодно или тепло».
 */
internal fun appetiteOf(
    fish: FishEntity,
    water: Double?,
    season: SeasonState?,
    conditions: SessionConditions
): Double {
    // Без воды судить не о чем: считаем рыбу умеренно голодной и говорим об
    // этом в советах, а не подставляем уверенную единицу.
    if (water == null) return NEUTRAL_APPETITE

    val thermal = when {
        water in fish.optMinTemp.toDouble()..fish.optMaxTemp.toDouble() -> 1.0
        water > fish.optMaxTemp -> falloff(water - fish.optMaxTemp, fish.absMaxTemp - fish.optMaxTemp)
        else -> falloff(fish.optMinTemp - water, fish.optMinTemp - fish.absMinTemp)
    }

    val seasonal = when (season?.phase) {
        SeasonPhase.DORMANT -> 0.0
        SeasonPhase.SPAWN -> 0.2
        SeasonPhase.POST_SPAWN -> 0.5
        SeasonPhase.WINTER -> 0.6
        SeasonPhase.PRE_SPAWN, SeasonPhase.AUTUMN -> 1.0
        else -> 0.9
    }

    // Балл клёва входит мягко: он про «ехать ли», а не про «сколько сыпать»,
    // и не должен один решать судьбу стола.
    val today = conditions.forecast.firstOrNull()?.score?.let { score ->
        SCORE_FLOOR + (1 - SCORE_FLOOR) * (score / MAX_SCORE)
    } ?: 1.0

    return (thermal * seasonal * today).coerceIn(0.0, 1.0)
}

/** Плавный спад от единицы до нуля на всю ширину запаса. */
private fun falloff(distance: Double, span: Float): Double {
    val width = span.toDouble().takeIf { it > 0 } ?: return 0.0
    return (1 - distance / width).coerceIn(0.0, 1.0)
}

/**
 * Чем вид занят в это время года.
 *
 * Формат тот же, что в карточке клёва: измеренное значение, тире, что оно
 * значит для этого вида с его порогом цифрой.
 */
private fun seasonAdvice(fish: FishEntity, season: SeasonState?): StrategyAdvice? {
    if (season == null) {
        return StrategyAdvice(
            title = "Сезон",
            value = "неизвестен",
            reason = "Истории воды меньше суток: направление сезона определить не по чему"
        )
    }

    val drift = "%s%.1f° за %d сут".format(
        if (season.driftC >= 0) "+" else "−",
        kotlin.math.abs(season.driftC),
        season.overDays
    )

    val reason = when (season.phase) {
        SeasonPhase.DORMANT -> "Вода %.1f° выше %.1f°, при которых вид впадает в оцепенение"
            .format(season.waterC, fish.dormantAboveC ?: 0f)

        SeasonPhase.WINTER -> "Вода %.1f° ниже %.1f°, с которых вид начинает кормиться"
            .format(season.waterC, fish.feedStartC ?: 0f)

        SeasonPhase.PRE_SPAWN -> "Вода %.0f°, $drift — идёт к нересту с %.0f°, ест впрок"
            .format(season.waterC, fish.spawnTempMinC ?: 0f)

        SeasonPhase.SPAWN -> "Вода %.0f° в нерестовой полосе %.0f–%.0f° на подъёме"
            .format(season.waterC, fish.spawnTempMinC ?: 0f, fish.spawnTempMaxC ?: 0f)

        SeasonPhase.POST_SPAWN -> season.daysSinceSpawn
            ?.let { "Нерест был %d сут назад, вид отходит %d".format(it, fish.postSpawnRecoveryDays) }
            ?: "Вид отходит после нереста"

        SeasonPhase.HEAT -> "Вода %.0f° выше оптимума %.0f–%.0f°"
            .format(season.waterC, fish.optMinTemp, fish.optMaxTemp)

        SeasonPhase.AUTUMN -> "Вода %.0f°, $drift — прошла оптимум сверху, рыба нагуливает"
            .format(season.waterC)

        SeasonPhase.SUMMER -> "Вода %.0f° в оптимуме %.0f–%.0f°, $drift"
            .format(season.waterC, fish.optMinTemp, fish.optMaxTemp)
    }

    return StrategyAdvice(title = "Сезон", value = season.phase.title, reason = reason)
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
    horizon != HORIZON_BOTTOM -> "Рыба стоит ${horizon.lowercase()}: донная снасть промолчит"
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
    val usual = horizonTitle(fish.defaultHorizon)
    // В заголовке совета «Дно», а в предложении нужен предлог: «вид и так у дна».
    val whereUsually = horizonText(fish.defaultHorizon)
    val warmForFish = water != null && water > fish.optMaxTemp
    val poorOxygen = oxygen != null && oxygenLevel(oxygen) != OxygenLevel.RICH &&
        oxygen < fish.oxygenComfortMgL + 1

    val reason = when {
        water == null -> "Вид держится $whereUsually; вода ещё не посчитана"

        warmForFish && poorOxygen ->
            "Вода %.1f° выше оптимума %.0f–%.0f° и кислорода %.1f мг/л: вид обычно %s, сейчас поднимается выше"
                .format(water, fish.optMinTemp, fish.optMaxTemp, oxygen, whereUsually)

        warmForFish ->
            "Вода %.1f° выше оптимума %.0f–%.0f°: вид обычно %s, сейчас поднимается выше"
                .format(water, fish.optMinTemp, fish.optMaxTemp, whereUsually)

        value != usual -> "Сумерки: вид поднимается выше обычного для себя горизонта"

        water < fish.optMinTemp ->
            "Вода %.0f° ниже оптимума %.0f–%.0f°: холодной рыбе не до подъёма, вид и так %s"
                .format(water, fish.optMinTemp, fish.optMaxTemp, whereUsually)

        else -> "Вода %.0f° в оптимуме %.0f–%.0f°: вид стоит там, где обычно"
            .format(water, fish.optMinTemp, fish.optMaxTemp)
    }

    val mismatch = method != null && method.horizon == "bottom" && value != HORIZON_BOTTOM
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
 * Начинается с того, где вид держится вообще: плотва стоит в толще, амур у
 * поверхности, лещ у дна. Раньше этого не спрашивали вовсе — горизонт считался
 * только по воде и кислороду, и всем видам в обычную погоду выпадало дно.
 * Амуру, которого ловят с поверхности на камыш, приложение советовало дно.
 *
 * Вода, кислород и свет остаются поправками: в прогретой воде рыба поднимается
 * над своим обычным горизонтом, и донная снасть кладёт насадку туда, где рыбы
 * нет.
 */
private fun horizonFor(
    fish: FishEntity,
    guild: Guild,
    water: Double?,
    oxygen: Double?,
    phase: LightPhase?
): String {
    val base = horizonTitle(fish.defaultHorizon)
    val warmForFish = water != null && water > fish.optMaxTemp
    val poorOxygen = oxygen != null && oxygenLevel(oxygen) != OxygenLevel.RICH &&
        oxygen < fish.oxygenComfortMgL + 1

    return when {
        // Верх поднимать некуда: вид и так там.
        base == HORIZON_TOP -> base
        warmForFish -> raise(base)
        poorOxygen && water != null && water > fish.optMaxTemp - HEAT_MARGIN_C -> raise(base)
        phase == LightPhase.DUSK && guild == Guild.PEACEFUL -> raise(base)
        else -> base
    }
}

/** На ступень выше обычного горизонта вида. */
private fun raise(horizon: String): String = when (horizon) {
    HORIZON_BOTTOM -> HORIZON_MID
    HORIZON_MID -> HORIZON_TOP
    else -> horizon
}

private fun baitAdvice(
    fish: FishEntity,
    guild: Guild,
    cold: Boolean,
    season: SeasonState?,
    conditions: SessionConditions,
    knowledge: KnowledgeCatalog,
    backup: Boolean
): StrategyAdvice? {
    // Сезон проверяется раньше гильдии: спящему налиму приманку подбирать так
    // же незачем, как спящему карпу насадку. У налима летом список «тёплых»
    // наживок пуст намеренно, и выдумывать вместо него нечего.
    if (season?.phase == SeasonPhase.DORMANT) return null

    if (guild == Guild.PREDATOR) return lureAdvice(fish, conditions, knowledge, backup)

    // На нагуле рыба тянется к животному, и говорить об этом, оставляя в руках
    // кукурузу, бессмысленно: пусть животная насадка станет запасной.
    val autumnShift = !cold && backup && season?.phase == SeasonPhase.AUTUMN
    val baits = when {
        autumnShift -> fish.baitsCold.decodeBaits()
        cold -> fish.baitsCold.decodeBaits()
        else -> fish.baitsWarm.decodeBaits()
    }
    val choice = baits.getOrNull(if (backup && !autumnShift) 1 else 0) ?: return null

    val water = conditions.waterShallowC
    val threshold = fish.coldTempThreshold.roundToInt()
    val base = if (water != null) {
        if (cold) {
            "Вода %.0f° ниже %d° для этого вида — берёт животное".format(water, threshold)
        } else {
            "Вода %.0f° выше %d° для этого вида — работает растительное и сладкое"
                .format(water, threshold)
        }
    } else {
        if (cold) "Холодная вода: вид берёт животное" else "Тёплая вода: работает растительное"
    }

    val seasonNote = when {
        autumnShift -> ". На нагуле рыба тянется к животному — держите его вторым"
        season?.phase == SeasonPhase.PRE_SPAWN -> ". Перед нерестом берёт крупно и жадно"
        season?.phase == SeasonPhase.SPAWN -> ". На нересте берёт редко: рассчитывать не стоит"
        season?.phase == SeasonPhase.POST_SPAWN -> ". После нереста берёт мелко и осторожно"
        season?.phase == SeasonPhase.AUTUMN -> ". На нагуле держите про запас животное"
        season?.phase == SeasonPhase.WINTER -> ". Зимой только животное и по одной штуке"
        else -> ""
    }

    return StrategyAdvice(
        title = if (backup) "Запасная насадка" else "Насадка",
        value = choice,
        reason = base + seasonNote
    )
}

/**
 * Приманка хищника: тип, цвет, размер и подача.
 *
 * Прозрачность оценивается по осадкам за сутки: ливень поднимает муть, и
 * тогда рыба ищет приманку боковой линией, а не глазами.
 */
private fun lureAdvice(
    fish: FishEntity,
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
    // Порог свой у каждого хищника: щука догоняет приманку с десяти
    // градусов, судаку нужно двенадцать. Общая константа врала обоим.
    val coldWater = (conditions.waterShallowC ?: 20.0) < fish.coldTempThreshold

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
    val water = conditions.waterShallowC
    val color = guide.colors.getOrNull(if (backup) 1 else 0) ?: guide.colors.firstOrNull().orEmpty()

    return StrategyAdvice(
        title = if (backup) "Запасная приманка" else "Приманка",
        value = listOf(type.name, color, guide.size).filter { it.isNotBlank() }.joinToString(", "),
        reason = listOf(
            water?.let {
                "Вода %.0f°, порог вида %.0f°".format(it, fish.coldTempThreshold)
            },
            guide.notes,
            "Подача: ${guide.action}"
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(". ")
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
    appetite: Double,
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
    if (conditions.season?.phase == SeasonPhase.DORMANT) {
        return StrategyAdvice(
            title = "Прикормка",
            value = "Не нужна",
            reason = "Вид в оцепенении: стол ему сейчас не нужен ни в каком объёме"
        )
    }

    val rule = (if (cold) fish.groundbaitCold else fish.groundbaitWarm).decodeGroundbait()
    val oxygen = conditions.oxygenMgL
    val water = conditions.waterShallowC
    val heatCut = water != null && water > fish.optMaxTemp - HEAT_MARGIN_C &&
        oxygen != null && oxygen < fish.oxygenComfortMgL + 1

    // Объём идёт от аппетита, а не от одного порога: он и так собран из воды,
    // сезона и сегодняшнего балла.
    val volume = when {
        heatCut -> "меньше обычного"
        else -> appetiteVolume(appetite, rule.volume)
    }

    val reason = buildList {
        if (heatCut) {
            add(
                "Вода %.0f° и кислорода %.1f мг/л: обильный стол сейчас во вред — рыба встанет над кормом"
                    .format(water, oxygen)
            )
        } else {
            add(appetiteReason(appetite, conditions.season))
        }
        add(rule.notes)
    }.map { it.trimEnd('.', ' ') }.filter { it.isNotBlank() }.joinToString(". ")

    return StrategyAdvice(
        title = "Прикормка",
        value = listOf(
            volume,
            fractionWord(rule.fraction),
            sweetWord(rule.sweetness),
            flavorText(rule.flavorProfile).takeIf { rule.flavorProfile != "none" }.orEmpty()
        ).filter { it.isNotBlank() }.joinToString(", "),
        reason = reason
    )
}

/**
 * Объём стола под аппетит.
 *
 * Справочник задаёт объём для вида в его обычном состоянии; аппетит говорит,
 * насколько рыба сегодня от этого состояния отстоит. Вверх объём не растёт:
 * перекормить проще, чем недокормить, и цена ошибки разная.
 */
private fun appetiteVolume(appetite: Double, ruleVolume: String): String = when {
    appetite < APPETITE_NONE -> "почти не кормить"
    appetite < APPETITE_LOW -> "мало корма"
    appetite < APPETITE_MID -> "умеренно"
    else -> volumeWord(ruleVolume)
}

/**
 * Почему стол именно такой.
 *
 * Объясняется решение, а не фаза вообще: сказать «стол можно держать полным»
 * рядом со словом «умеренно» — значит противоречить себе в одной строке.
 */
private fun appetiteReason(appetite: Double, season: SeasonState?): String {
    val percent = (appetite * 100).roundToInt()
    val phase = season?.phase?.title?.lowercase()

    val decision = when {
        appetite < APPETITE_NONE -> "кормить почти нечем: рыба сейчас не ест"
        appetite < APPETITE_LOW -> "стол малый: лишний корм насытит рыбу раньше крючка"
        appetite < APPETITE_MID -> "стол умеренный: до полного рыба сегодня не доедает"
        else -> "стол можно держать полным"
    }

    return if (phase != null) "Аппетит $percent %, $phase — $decision" else "Аппетит $percent % — $decision"
}

/**
 * Схема закорма под цель, воду и размер водоёма.
 *
 * Правило о холодной воде старше спора о цели: когда рыба ест мало, любой
 * стол, кроме точечного, работает против рыболова — потому схема на
 * холодную воду одна на обе цели.
 */
private fun baitingPlanFor(
    goal: CatchGoal,
    cold: Boolean,
    largeWater: Boolean,
    knowledge: KnowledgeCatalog
): BaitingPlan? {
    val water = if (cold) "cold" else "warm"
    val size = if (largeWater) "large" else "small"

    return knowledge.baitingPlans.firstOrNull {
        it.water == water && it.goal == goal.key && it.waterSize == size
    }
        ?: knowledge.baitingPlans.firstOrNull {
            it.water == water && it.goal == goal.key && it.waterSize == null
        }
        ?: knowledge.baitingPlans.firstOrNull { it.water == water && it.goal == "any" }
        ?: knowledge.baitingPlans.firstOrNull { it.goal == goal.key }
}

/**
 * Как рассыпать корм.
 *
 * Справочник вида отвечает, из чего делать стол; схема — как он ляжет на
 * дно. Один и тот же корм ковром и точкой зовёт разную рыбу.
 */
private fun baitingAdvice(
    plan: BaitingPlan?,
    input: SessionPlanInput,
    cold: Boolean,
    appetite: Double,
    method: FishingMethod?
): StrategyAdvice? {
    if (plan == null) return null
    if (!input.hasGroundbait || method?.groundbait == false) return null

    val reason = buildList {
        add(plan.notes)
        if (plan.primeDays > 0) {
            add(
                "Точка готовится ${plan.primeDays} дней до выезда: за один вечер такой " +
                    "программы не сделать"
            )
        }
        if (input.goal == CatchGoal.TROPHY && plan.goal != CatchGoal.TROPHY.key && cold) {
            add("Трофейные схемы оставьте на тёплую воду: сейчас решает не объём стола, а точность")
        }
        if (appetite < APPETITE_MID) {
            add(
                "Схема остаётся, объём — нет: при аппетите %d %% сыпьте вполовину от неё"
                    .format((appetite * 100).roundToInt())
            )
        }
    }.map { it.trimEnd('.', ' ') }.filter { it.isNotBlank() }.joinToString(". ")

    // Объём схемы не должен спорить с объёмом стола строкой выше.
    val volume = if (appetite < APPETITE_MID) {
        appetiteVolume(appetite, plan.volume)
    } else {
        volumeWord(plan.volume)
    }

    return StrategyAdvice(
        title = "Схема закорма",
        value = listOf(plan.name, volume).filter { it.isNotBlank() }.joinToString(", "),
        reason = reason
    )
}

/**
 * Размер и твёрдость насадки.
 *
 * Единственный отбор, который работает до поклёвки: мелочь, лещ и раки
 * просто не справляются с крупной сушёной насадкой, а рыба с развитыми
 * глоточными зубами справляется.
 */
private fun selectionAdvice(plan: BaitingPlan?, appetite: Double): StrategyAdvice? {
    val size = plan?.baitSizeMm?.takeIf { it.isNotBlank() } ?: return null

    // Вялая рыба не берёт крупное: она не станет тратить силы на то, что
    // тяжело всосать. Схема остаётся, а размер уходит вниз.
    if (appetite < APPETITE_LOW) {
        return StrategyAdvice(
            title = "Размер насадки",
            value = "мельче схемы, одна-две штуки",
            reason = "Аппетит %d %%: крупную насадку вялая рыба не возьмёт, как её ни суши"
                .format((appetite * 100).roundToInt())
        )
    }

    return StrategyAdvice(
        title = "Размер насадки",
        value = if (plan.hardened) "$size, сушить до каменной твёрдости" else size,
        reason = if (plan.hardened) {
            "Такую насадку мелкий карп, лещ и раки не осилят — останется только крупная рыба"
        } else {
            "Мелкая насадка даёт быстрые поклёвки активного молодняка: рыбы больше, размер меньше"
        }
    )
}

/**
 * Монтаж под способ.
 *
 * Сложный монтаж не ловит больше — он просто чаще подводит. Здесь принцип
 * и вес грузила, с которого способ начинает засекать рыбу сам.
 */
private fun rigAdvice(method: FishingMethod?): StrategyAdvice? {
    val rig = method?.rig?.takeIf { it.isNotBlank() } ?: return null

    return StrategyAdvice(
        title = "Монтаж",
        value = if (method.minLeadG > 0) {
            "${method.name}, грузило от ${method.minLeadG} г"
        } else {
            method.name
        },
        reason = rig
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
    // Сезон идёт первым: если рыба спит или на нересте, всё остальное —
    // подробности о снасти, которая сегодня не понадобится.
    when (conditions.season?.phase) {
        SeasonPhase.DORMANT -> add(
            "Вода %.1f° выше %.1f°: %s в оцепенении и почти не питается — сегодня стоит ехать за кем-то другим"
                .format(
                    conditions.season.waterC,
                    fish.dormantAboveC ?: 0f,
                    fish.name.lowercase()
                )
        )

        SeasonPhase.SPAWN -> add(
            "Вода %.0f° в нерестовой полосе %.0f–%.0f° на подъёме: рыба на нересте, " +
                "и в большинстве регионов в это время действует запрет — проверьте правила своей области"
                    .format(
                        conditions.season.waterC,
                        fish.spawnTempMinC ?: 0f,
                        fish.spawnTempMaxC ?: 0f
                    )
        )

        SeasonPhase.POST_SPAWN -> add(
            "Рыба отходит после нереста: поклёвки будут редкими ещё до %d суток"
                .format(fish.postSpawnRecoveryDays)
        )

        else -> Unit
    }

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
    // Про оцепенение уже сказано выше своими словами — не повторяем дважды.
    if (water != null && water > fish.absMaxTemp &&
        conditions.season?.phase != SeasonPhase.DORMANT
    ) {
        add(
            "Вода %.0f° выше предела вида %.0f°: сегодня стоит ехать за кем-то другим"
                .format(water, fish.absMaxTemp)
        )
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

/**
 * Пороги аппетита: где стол меняется качественно, а не на четверть горсти.
 *
 * Ниже [APPETITE_NONE] кормить незачем вовсе, ниже [APPETITE_LOW] рыба не
 * возьмёт крупную насадку, ниже [APPETITE_MID] полный стол насытит её раньше
 * крючка.
 */
private const val APPETITE_NONE = 0.15
private const val APPETITE_LOW = 0.35
private const val APPETITE_MID = 0.65

/** Аппетит, когда воду ещё не посчитали: не уверенная единица и не ноль. */
internal const val NEUTRAL_APPETITE = 0.6

/** Насколько мягко балл клёва влияет на стол: от него остаётся не меньше этого. */
private const val SCORE_FLOOR = 0.6

/** Верх шкалы клёва. */
private const val MAX_SCORE = 100.0

/** Горизонты словами: те же строки, что показывает совет. */
private const val HORIZON_BOTTOM = "Дно"
private const val HORIZON_MID = "Полводы"
private const val HORIZON_TOP = "Верх"

/** За сколько градусов до предела вида стол пора урезать. */
private const val HEAT_MARGIN_C = 2.0

/** Насколько лучший час должен опережать нынешний, чтобы его ждать. */
private const val WINDOW_DIFFERENCE = 10

/** Разрыв между слоями, ниже которого выбирать глубину незачем. */
private const val LAYER_DIFFERENCE = 5

/** На сколько часов вперёд расписывается ход суток. */
private const val HOURS_AHEAD = 24
