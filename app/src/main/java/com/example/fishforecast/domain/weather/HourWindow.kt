package com.example.fishforecast.domain.weather

import java.time.Duration
import java.time.LocalDateTime

/**
 * Окно часов вокруг текущего момента: сколько-то назад и сколько-то вперёд.
 *
 * Прогноз без прошлого отвечает только на вопрос «что будет». Но рыболову
 * важнее «что происходит»: давление, которое падает третий час подряд,
 * читается лишь в сравнении с тем, что было. История уже лежит в базе —
 * её просто никто не показывал.
 */
data class HourWindow<T>(
    val items: List<T>,
    /** Где в списке текущий час; -1, если его в окне нет. */
    val nowIndex: Int
) {
    val isEmpty: Boolean get() = items.isEmpty()

    /** Час считается прошедшим, если стоит левее отметки «сейчас». */
    fun isPast(index: Int): Boolean = nowIndex >= 0 && index < nowIndex
}

/**
 * Вырезает окно вокруг текущего часа.
 *
 * Ряд может не дотягиваться ни назад, ни вперёд — окно просто окажется
 * короче, а отметка «сейчас» сместится. Выдумывать недостающие часы нельзя:
 * пустое место честнее нарисованной линии.
 */
fun <T> hourWindow(
    items: List<T>,
    hoursBack: Int,
    hoursForward: Int,
    now: LocalDateTime = LocalDateTime.now(),
    time: (T) -> String
): HourWindow<T> {
    if (items.isEmpty()) return HourWindow(emptyList(), -1)

    val sorted = items.sortedBy(time)
    val current = sorted.indexOfFirst { entry ->
        val moment = runCatching { LocalDateTime.parse(time(entry)) }.getOrNull()
        moment != null && !moment.isBefore(now.withMinute(0).withSecond(0).withNano(0))
    }

    // Все часы в прошлом — значит прогноз устарел; показываем его хвост.
    if (current < 0) {
        val tail = sorted.takeLast(hoursBack + 1)
        return HourWindow(tail, tail.lastIndex)
    }

    val from = (current - hoursBack).coerceAtLeast(0)
    val to = (current + hoursForward).coerceAtMost(sorted.lastIndex)

    return HourWindow(
        items = sorted.subList(from, to + 1),
        nowIndex = current - from
    )
}

/** Насколько час отстоит от текущего: для подписей «3 часа назад». */
fun hoursFromNow(time: String, now: LocalDateTime = LocalDateTime.now()): Long? {
    val moment = runCatching { LocalDateTime.parse(time) }.getOrNull() ?: return null
    return Duration.between(now, moment).toHours()
}
