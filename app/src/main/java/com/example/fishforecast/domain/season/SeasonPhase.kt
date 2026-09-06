package com.example.fishforecast.domain.season

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.domain.water.WaterHour
import java.time.Duration
import java.time.LocalDateTime

/**
 * Фаза сезона вида.
 *
 * Решает вода, а не календарь. Одно и то же число на термометре значит
 * противоположное весной и осенью: в мае четырнадцать градусов — это подъём к
 * нересту, в сентябре те же четырнадцать — нагул перед зимой. Разницу даёт
 * направление, а его приложение и так считает: история воды хранится за неделю
 * назад.
 *
 * Календарь понадобился бы только для нерестового запрета, но он свой в каждом
 * регионе, и врать про него хуже, чем промолчать: фаза нереста просто
 * предупреждает, что запрет возможен.
 */
enum class SeasonPhase(val title: String) {
    /** Вода вне полосы жизни вида: налим летом, амур в холоде. */
    DORMANT("Оцепенение"),

    /** Ниже порога кормления: рыба жива, но ест редко и мало. */
    WINTER("Зимнее стояние"),

    /** Вода растёт и подходит к нересту снизу — лучший жор года. */
    PRE_SPAWN("Преднерестовый жор"),

    /** В нерестовой полосе на подъёме: рыбе не до еды. */
    SPAWN("Нерест"),

    /** Первые дни после нереста: рыба отходит. */
    POST_SPAWN("После нереста"),

    /** В оптимуме, обычная кормёжка. */
    SUMMER("Лето"),

    /** Выше оптимума: душно, рыба поднимается над дном. */
    HEAT("Жара"),

    /** Вода падает через оптимум вниз: нагул перед зимой. */
    AUTUMN("Осенний нагул")
}

/**
 * Фаза со всем, что понадобилось, чтобы её объяснить.
 *
 * Причина хранится рядом с самой фазой: совет без причины проверить нельзя, а
 * рыбалка это ровно проверка.
 */
data class SeasonState(
    val phase: SeasonPhase,
    /** Температура воды, по которой решали, °C. */
    val waterC: Double,
    /** Ход воды за окно наблюдения, °C: со знаком. */
    val driftC: Double,
    /** За сколько суток набежал этот ход. */
    val overDays: Int,
    /** Сколько суток назад вода вышла из нерестовой полосы вверх; null — не выходила. */
    val daysSinceSpawn: Int? = null
) {
    val rising: Boolean get() = driftC >= DRIFT_STEP_C
    val falling: Boolean get() = driftC <= -DRIFT_STEP_C
}

/** Час истории с уже разобранным временем: разбирать его заново дорого. */
private data class Moment(val at: LocalDateTime, val temperature: Double)

/**
 * История воды до текущего момента, разобранная один раз.
 *
 * Справочник считается сразу по десяти видам, история хранится за неделю, и
 * каждый разбор `LocalDateTime.parse` на этом умножается. Поэтому время
 * разбирается одним проходом, а всё остальное работает уже с готовым.
 */
private fun List<WaterHour>.past(now: LocalDateTime): List<Moment> = mapNotNull { hour ->
    runCatching { LocalDateTime.parse(hour.time) }.getOrNull()
        ?.takeIf { !it.isAfter(now) }
        ?.let { Moment(it, hour.temperature) }
}

/**
 * Куда идёт вода за несколько суток.
 *
 * Окно намеренно длинное: суточный ход воды — это день и ночь, а сезон виден
 * только на масштабе суток. Берётся столько, сколько есть в истории, но не
 * больше [DRIFT_WINDOW_DAYS].
 */
fun waterDrift(history: List<WaterHour>, now: LocalDateTime): Pair<Double, Int>? =
    driftOf(history.past(now))

private fun driftOf(past: List<Moment>): Pair<Double, Int>? {
    if (past.size < 2) return null

    val last = past.last()
    val earliest = last.at.minusDays(DRIFT_WINDOW_DAYS.toLong())
    val first = past.firstOrNull { !it.at.isBefore(earliest) } ?: return null

    val days = Duration.between(first.at, last.at).toHours() / HOURS_PER_DAY
    if (days < 1) return null
    return (last.temperature - first.temperature) to days.toInt()
}

/**
 * Фаза сезона вида по воде и её ходу.
 *
 * Порядок проверок — от жёсткого к мягкому: оцепенение отменяет всё
 * остальное, нерест важнее оптимума, и только в конце остаётся обычное лето.
 *
 * @param history ход воды того слоя, где рыба стоит; нужна история за сутки и
 *        дольше, иначе направление не определить и фазы не будет.
 */
fun seasonPhaseOf(
    fish: FishEntity,
    history: List<WaterHour>,
    now: LocalDateTime = LocalDateTime.now()
): SeasonState? {
    val past = history.past(now)
    val water = past.lastOrNull()?.temperature ?: return null
    val (drift, days) = driftOf(past) ?: return null

    val base = SeasonState(
        phase = SeasonPhase.SUMMER,
        waterC = water,
        driftC = drift,
        overDays = days,
        daysSinceSpawn = daysSinceSpawn(fish, past, now)
    )

    // Оцепенение сильнее всего: налиму летом не поможет ни давление, ни ветер.
    fish.dormantAboveC?.let { limit ->
        if (water > limit) return base.copy(phase = SeasonPhase.DORMANT)
    }
    fish.feedStartC?.let { start ->
        if (water < start) return base.copy(phase = SeasonPhase.WINTER)
    }

    val spawnMin = fish.spawnTempMinC
    val spawnMax = fish.spawnTempMaxC
    if (spawnMin != null && spawnMax != null) {
        // Нерест — только на подъёме: осенью вода проходит ту же полосу вниз,
        // и это уже нагул, а не икра.
        if (base.rising && water >= spawnMin && water <= spawnMax) {
            return base.copy(phase = SeasonPhase.SPAWN)
        }
        if (base.rising && water < spawnMin && water >= spawnMin - PRE_SPAWN_MARGIN_C) {
            return base.copy(phase = SeasonPhase.PRE_SPAWN)
        }
        // Отходят только после нереста, то есть выйдя из полосы вверх. Осенью
        // вода проходит ту же полосу вниз, и «только что отнерестилась» про
        // сентябрьского карпа — неправда.
        val since = base.daysSinceSpawn
        if (water > spawnMax && since != null && since <= fish.postSpawnRecoveryDays) {
            return base.copy(phase = SeasonPhase.POST_SPAWN)
        }
    }

    return when {
        water > fish.optMaxTemp -> base.copy(phase = SeasonPhase.HEAT)
        base.falling && water < fish.optMaxTemp -> base.copy(phase = SeasonPhase.AUTUMN)
        else -> base
    }
}

/**
 * Сколько суток назад вода в последний раз стояла в нерестовой полосе.
 *
 * История хранится за неделю, а восстановление длится дольше: если нерест
 * прошёл раньше, чем начинается история, узнать об этом неоткуда — и тогда
 * возвращается null, а не догадка.
 */
private fun daysSinceSpawn(
    fish: FishEntity,
    past: List<Moment>,
    now: LocalDateTime
): Int? {
    val min = fish.spawnTempMinC ?: return null
    val max = fish.spawnTempMaxC ?: return null

    val inBand = past.lastOrNull { it.temperature in min..max } ?: return null

    val hours = Duration.between(inBand.at, now).toHours()
    if (hours < 0) return null
    return (hours / HOURS_PER_DAY).toInt()
}

/** Ниже этого ход воды за несколько суток считается шумом, °C. */
private const val DRIFT_STEP_C = 0.5

/** Сколько суток назад смотрим, чтобы понять направление сезона. */
private const val DRIFT_WINDOW_DAYS = 3

/**
 * Насколько ниже нерестовой полосы начинается преднерестовый жор, °C.
 *
 * Три градуса — это примерно неделя прогрева весной: столько рыба и кормится
 * перед тем, как встать на нерест.
 */
private const val PRE_SPAWN_MARGIN_C = 3.0

private const val HOURS_PER_DAY = 24
