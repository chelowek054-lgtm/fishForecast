package com.example.fishforecast.domain.light

import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.domain.fish.Guild
import java.time.Duration
import java.time.LocalDateTime

/**
 * Фаза света: главный ритм, по которому живёт рыба.
 *
 * Восход и закат приложение знает давно, но в расчёт они не входили. А ведь
 * это и есть тот разделитель, которого не хватало: в одной и той же воде
 * судак кормится в сумерках, плотва днём, а налим ночью. Без света все виды
 * получали один и тот же балл — различать их было нечем.
 *
 * Границы считаются от восхода и заката, а не от часов: летний рассвет в
 * четыре утра и зимний в девять — это одна и та же фаза.
 */
enum class LightPhase(val title: String) {
    NIGHT("ночь"),
    DAWN("рассвет"),
    MORNING("утро"),
    DAY("день"),
    EVENING("вечер"),
    DUSK("сумерки");

    /** Ключ в профиле активности вида из справочника. */
    val key: String
        get() = when (this) {
            NIGHT -> "night"
            DAWN -> "dawn"
            MORNING -> "morning"
            DAY -> "day"
            EVENING -> "evening"
            DUSK -> "dusk"
        }
}

/**
 * Какая фаза света в этот час.
 *
 * Зори — узкие окна вокруг восхода и заката; час после рассвета ещё утро,
 * час перед закатом уже вечер. Без данных о солнце фазы определить нечем,
 * и функция честно возвращает null, а не выдумывает середину дня.
 */
fun lightPhaseAt(time: LocalDateTime, sun: DailySunEntity?): LightPhase? {
    val sunrise = sun?.sunrise?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        ?: return null
    val sunset = sun.sunset.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        ?: return null

    val toSunrise = Duration.between(time, sunrise).toMinutes()
    val toSunset = Duration.between(time, sunset).toMinutes()

    return when {
        // Зори считаются от самого события в обе стороны: клевать начинает
        // ещё в темноте и заканчивает уже при свете.
        kotlin.math.abs(toSunrise) <= TWILIGHT_MINUTES -> LightPhase.DAWN
        kotlin.math.abs(toSunset) <= TWILIGHT_MINUTES -> LightPhase.DUSK
        toSunrise > 0 || toSunset < 0 -> LightPhase.NIGHT
        toSunrise < -MORNING_MINUTES && toSunset > EVENING_MINUTES -> LightPhase.DAY
        toSunset <= EVENING_MINUTES -> LightPhase.EVENING
        else -> LightPhase.MORNING
    }
}

/**
 * Профиль активности по умолчанию, когда у вида своего нет.
 *
 * Хищник охотится глазами: его пики на зорях, когда добыче плохо видно, а
 * ему ещё хватает света. Мирная рыба ищет корм и потому размазана по
 * светлому времени, зато ночью почти стоит.
 */
fun defaultLightActivity(guild: Guild): Map<String, Double> = when (guild) {
    Guild.PREDATOR -> mapOf(
        "night" to 0.3,
        "dawn" to 1.0,
        "morning" to 0.8,
        "day" to 0.5,
        "evening" to 0.8,
        "dusk" to 1.0
    )

    Guild.PEACEFUL -> mapOf(
        "night" to 0.4,
        "dawn" to 0.9,
        "morning" to 1.0,
        "day" to 0.7,
        "evening" to 0.9,
        "dusk" to 0.8
    )
}

/**
 * Насколько вид активен в эту фазу. Свой профиль старше умолчания гильдии:
 * налим и сом ломают любое правило про хищника на зорях.
 */
fun lightActivity(
    phase: LightPhase,
    ownProfile: Map<String, Double>,
    guild: Guild
): Double {
    val profile = ownProfile.takeIf { it.isNotEmpty() } ?: defaultLightActivity(guild)
    return profile[phase.key]?.coerceIn(0.0, 1.0) ?: defaultLightActivity(guild).getValue(phase.key)
}

/** Полчаса до и после события — то, что рыболовы и называют зорькой. */
private const val TWILIGHT_MINUTES = 45L

/** Сколько после рассвета ещё считается утром. */
private const val MORNING_MINUTES = 180L

/** За сколько до заката начинается вечер. */
private const val EVENING_MINUTES = 180L
