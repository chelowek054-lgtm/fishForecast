package com.example.fishforecast.domain.alert

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Когда звать рыболова на воду.
 *
 * Уведомление — единственное, что приложение делает без спроса, и потому
 * единственное, чем оно способно надоесть. Правило простое: звать, только если
 * рыболов и захочет поехать, и физически успеет. Всё остальное он увидит сам,
 * когда откроет приложение.
 *
 * Каждый отказ назван словами: молчание без причины отладить нельзя.
 */
sealed interface AlertDecision {
    /** Звать: до окна столько-то, дорога занимает столько-то. */
    data class Notify(val leadTime: Duration, val travel: Duration?) : AlertDecision

    /** Молчать, и вот почему. */
    data class Skip(val reason: String) : AlertDecision
}

/** Что известно о прошлых зовах: без этого одно окно объявляется четырежды. */
data class AlertHistory(
    val lastNotifiedAt: LocalDateTime? = null,
    /** Время окна, о котором уже звали, — чтобы не звать о нём снова. */
    val lastWindowTime: String? = null
)

/**
 * Решает, стоит ли уведомление беспокойства.
 *
 * @param windowTime начало окна хорошего клёва.
 * @param windowScore балл этого окна.
 * @param currentScore балл ближайшего часа: если и сейчас хорошо, звать не о чем.
 * @param travel сколько ехать до воды; null — неизвестно, и тогда запас берётся
 *        с потолка, но в большую сторону.
 */
fun decideAlert(
    windowTime: LocalDateTime,
    windowScore: Int,
    currentScore: Int?,
    now: LocalDateTime,
    travel: Duration?,
    history: AlertHistory
): AlertDecision {
    if (history.lastWindowTime == windowTime.toString()) {
        return AlertDecision.Skip("об этом окне уже звали")
    }
    history.lastNotifiedAt?.let { last ->
        val since = Duration.between(last, now)
        if (since < COOLDOWN) {
            return AlertDecision.Skip("с прошлого раза прошло меньше ${COOLDOWN.toHours()} ч")
        }
    }

    // Ночью человек спит, а не собирается на рыбалку. Окно никуда не денется:
    // следующая проверка застанет его утром и позовёт тогда.
    val hour = now.toLocalTime()
    if (hour >= QUIET_FROM || hour < QUIET_UNTIL) {
        return AlertDecision.Skip("тихие часы с $QUIET_FROM до $QUIET_UNTIL")
    }

    val lead = Duration.between(now, windowTime)
    if (lead.isNegative || lead.isZero) {
        return AlertDecision.Skip("окно уже началось")
    }
    if (lead > MAX_LEAD) {
        return AlertDecision.Skip("до окна больше ${MAX_LEAD.toHours()} ч — рано звать")
    }

    // Главное правило: успеет ли доехать. Дорога плюс сборы должны уместиться
    // в оставшееся время, иначе зов — это не помощь, а сожаление.
    val needed = (travel ?: UNKNOWN_TRAVEL) + GATHERING
    if (lead < needed) {
        return AlertDecision.Skip(
            "не успеть: до окна ${lead.toHours()} ч, а нужно ${needed.toHours()}"
        )
    }

    // Если и сейчас клюёт не хуже, повод не в клёве, а в желании приложения
    // о себе напомнить.
    if (currentScore != null && windowScore - currentScore < MIN_IMPROVEMENT) {
        return AlertDecision.Skip(
            "сейчас $currentScore, в окне $windowScore — не та разница, чтобы будить"
        )
    }

    return AlertDecision.Notify(leadTime = lead, travel = travel)
}

/**
 * Сколько ехать до воды по прямой с поправкой на дороги.
 *
 * Маршрутизатора у приложения нет и не будет: он требует сети, а приложение
 * офлайн-первое. Прямая с коэффициентом извилистости ошибается, но ошибается
 * предсказуемо и в понятную сторону — этого хватает, чтобы отличить «полчаса»
 * от «полдня».
 */
fun travelTime(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double,
    toLongitude: Double
): Duration {
    val straightKm = distanceKm(fromLatitude, fromLongitude, toLatitude, toLongitude)
    val roadKm = straightKm * ROAD_WINDING
    val hours = roadKm / AVERAGE_SPEED_KMH
    return Duration.ofMinutes((hours * MINUTES_PER_HOUR).toLong())
}

/** Расстояние по большому кругу, км. */
fun distanceKm(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double,
    toLongitude: Double
): Double {
    val dLat = Math.toRadians(toLatitude - fromLatitude)
    val dLon = Math.toRadians(toLongitude - fromLongitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(fromLatitude)) * cos(Math.toRadians(toLatitude)) *
        sin(dLon / 2) * sin(dLon / 2)
    return 2 * EARTH_RADIUS_KM * asin(min(1.0, sqrt(a)))
}

/** Не чаще одного зова в эти часы, каким бы хорошим ни был прогноз. */
private val COOLDOWN: Duration = Duration.ofHours(12)

/** Ночью не звать. */
private val QUIET_FROM: LocalTime = LocalTime.of(22, 0)
private val QUIET_UNTIL: LocalTime = LocalTime.of(7, 0)

/**
 * Дальше этого горизонта звать рано.
 *
 * Про окно за сутки человек забудет, а до него ещё успеет измениться прогноз.
 * Двенадцать часов — это «сегодня вечером» или «завтра утром»: срок, на который
 * планы строят всерьёз.
 */
private val MAX_LEAD: Duration = Duration.ofHours(12)

/** Сколько закладываем на сборы: снасти, прикормка, дорога до машины. */
private val GATHERING: Duration = Duration.ofMinutes(45)

/**
 * Сколько считаем дорогой, когда своего места не знаем.
 *
 * Без разрешения на геолокацию расстояние неизвестно. Час — осторожная
 * середина: он отсекает окна, до которых точно не успеть, и не отменяет те,
 * до которых ехать недалеко.
 */
private val UNKNOWN_TRAVEL: Duration = Duration.ofHours(1)

/** Насколько окно должно быть лучше нынешнего часа, чтобы стоить звонка. */
private const val MIN_IMPROVEMENT = 12

/** Дороги длиннее прямой: типовой коэффициент для средней полосы. */
private const val ROAD_WINDING = 1.35

/** Средняя скорость с учётом города и просёлка, км/ч. */
private const val AVERAGE_SPEED_KMH = 55.0

private const val EARTH_RADIUS_KM = 6371.0
private const val MINUTES_PER_HOUR = 60.0
