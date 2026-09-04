package com.example.fishforecast.domain.bite

import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.knowledge.ObservationType
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Наблюдение с берега вместе со временем, когда его сделали.
 *
 * Тип приходит из словаря знаний: там у наблюдения записано, кого оно
 * касается, насколько сдвигает оценку и сколько часов действует.
 */
data class ActiveObservation(
    val type: ObservationType,
    val notedAt: LocalDateTime
)

/**
 * Поправка к оценке по тому, что рыболов видел своими глазами.
 *
 * Наблюдение — факт, а прогноз — расчёт, поэтому факт весомее. Но живёт он
 * недолго: бой малька через два часа уже в другом углу водоёма, а радужная
 * плёнка над гнилым илом продержится полсуток. Отсюда затухание: сразу после
 * отметки наблюдение работает целиком, к концу своего срока — уже ничем.
 *
 * Влияние множится с остальными ограничителями, а не складывается с условиями:
 * увиденный бой хищника не «добавляет баллов», он говорит, что рыба здесь и
 * сейчас кормится, — и это меняет всё остальное разом.
 */
fun observationFactor(
    observations: List<ActiveObservation>,
    guild: Guild,
    hourTime: String
): BiteFactor? {
    if (observations.isEmpty()) return null
    val moment = runCatching { LocalDateTime.parse(hourTime) }.getOrNull() ?: return null

    val applied = observations.mapNotNull { observation ->
        val fade = fadeAt(observation, moment) ?: return@mapNotNull null
        if (!observation.type.appliesTo(guild)) return@mapNotNull null
        observation to fade
    }
    if (applied.isEmpty()) return null

    val shift = applied.sumOf { (observation, fade) -> observation.type.effect * fade }
    val value = (1.0 + shift).coerceIn(MIN_EFFECT, MAX_EFFECT)

    return BiteFactor(
        name = "Замечено",
        value = value,
        weight = 0.0,
        limiting = true,
        comment = applied.joinToString("; ") { (observation, fade) ->
            observation.type.name.replaceFirstChar { it.lowercase() } +
                " — " + describe(observation, fade)
        }
    )
}

/**
 * Насколько наблюдение ещё в силе для этого часа: 1.0 в момент отметки, 0 к
 * концу срока. Часы до отметки наблюдение не касается вовсе — оно рассказывает
 * о том, что уже произошло, а не о том, что будет.
 */
private fun fadeAt(observation: ActiveObservation, moment: LocalDateTime): Double? {
    val hours = observation.type.hours
    if (hours <= 0) return null

    val elapsed = Duration.between(observation.notedAt, moment).toMinutes() / MINUTES_PER_HOUR
    if (elapsed < 0 || elapsed >= hours) return null
    return 1.0 - elapsed / hours
}

/** Кого касается наблюдение: `any` касается всех. */
private fun ObservationType.appliesTo(guild: Guild): Boolean = when (this.guild) {
    "predator" -> guild == Guild.PREDATOR
    "peaceful" -> guild == Guild.PEACEFUL
    else -> true
}

private fun describe(observation: ActiveObservation, fade: Double): String {
    val left = (observation.type.hours * fade).roundToInt()
    val strength = when {
        fade >= 0.75 -> "свежее наблюдение"
        fade >= 0.35 -> "ещё в силе"
        else -> "почти выдохлось"
    }
    return if (left >= 1) "$strength, около $left ч" else "$strength, вот-вот истечёт"
}

/** Даже самое дурное наблюдение не обнуляет шанс, самое доброе не заменяет погоду. */
private const val MIN_EFFECT = 0.4
private const val MAX_EFFECT = 1.6

private const val MINUTES_PER_HOUR = 60.0
