package com.example.fishforecast.domain.bite

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Часть суток.
 *
 * Границы обычные, а не астрономические: рыболов планирует выезд словами
 * «в ночь», «на утреннюю зорьку», «после работы». Зори при этом попадают
 * внутрь частей, а не образуют свои — иначе клеток стало бы шесть, и таблица
 * перестала бы читаться с одного взгляда.
 */
enum class PartOfDay(val title: String, val fromHour: Int, val untilHour: Int) {
    NIGHT("Ночь", 0, 6),
    MORNING("Утро", 6, 12),
    DAY("День", 12, 18),
    EVENING("Вечер", 18, 24);

    companion object {
        fun of(hour: Int): PartOfDay = entries.first { hour >= it.fromHour && hour < it.untilHour }
    }
}

/**
 * Клёв в одной части суток.
 *
 * Хранятся два числа. Средний балл отвечает на вопрос «стоит ли ехать на эти
 * шесть часов»: рыболов проводит там всю часть, а не один час. Лучший час
 * отвечает на другой — «к какому времени быть на воде». Одного числа для
 * обоих вопросов не хватает: ровные шестьдесят и провал с коротким всплеском
 * дают одинаковое среднее, но ехать за ними надо по-разному.
 */
data class PartActivity(
    val part: PartOfDay,
    /** Средний балл по части, 0..100. */
    val score: Int,
    /** Лучший час внутри части, «HH:mm». */
    val bestHour: String,
    val bestScore: Int,
    /** Сколько часов части попало в прогноз: часть бывает неполной. */
    val hours: Int
) {
    val level: BiteLevel get() = BiteLevel.fromScore(score)
}

/** Сутки, разбитые на части. Пустые части в список не попадают. */
data class DayActivity(
    val date: LocalDate,
    val parts: List<PartActivity>
) {
    /** Лучшая часть суток: по ней день и сравнивают с соседними. */
    val best: PartActivity? get() = parts.maxByOrNull { it.score }
}

/**
 * Клёв на неделю вперёд по частям суток.
 *
 * Почасовой график отвечает на вопрос «ехать ли сегодня». На вопрос «когда
 * взять отгул» он не отвечает: двадцать четыре часа вперёд — это не планы,
 * это сегодняшний вечер. Здесь тот же расчёт, свёрнутый до четырёх клеток в
 * сутки, — столько, сколько помещается в решение.
 *
 * Прошедшие части отбрасываются: планировать вчерашнее утро не нужно, а
 * пустая клетка честнее нарисованной задним числом.
 *
 * @param from момент, с которого планируем; часы раньше него не считаются.
 * @param days сколько суток показывать, считая сегодняшние остатки.
 */
fun weekActivity(
    forecast: List<BiteForecast>,
    from: LocalDateTime,
    days: Int = DEFAULT_DAYS
): List<DayActivity> {
    if (forecast.isEmpty()) return emptyList()

    val ahead = forecast.mapNotNull { hour ->
        val time = runCatching { LocalDateTime.parse(hour.time) }.getOrNull()
            ?: return@mapNotNull null
        if (time.isBefore(from.withMinute(0).withSecond(0).withNano(0))) null else time to hour
    }
    if (ahead.isEmpty()) return emptyList()

    return ahead
        .groupBy { (time, _) -> time.toLocalDate() }
        .toSortedMap()
        .entries
        .take(days)
        .map { (date, hours) ->
            DayActivity(
                date = date,
                parts = hours
                    .groupBy { (time, _) -> PartOfDay.of(time.hour) }
                    .map { (part, inPart) -> summarize(part, inPart) }
                    .sortedBy { it.part.ordinal }
            )
        }
}

private fun summarize(
    part: PartOfDay,
    hours: List<Pair<LocalDateTime, BiteForecast>>
): PartActivity {
    val best = hours.maxBy { (_, forecast) -> forecast.score }
    return PartActivity(
        part = part,
        score = hours.sumOf { (_, forecast) -> forecast.score } / hours.size,
        bestHour = "%02d:00".format(best.first.hour),
        bestScore = best.second.score,
        hours = hours.size
    )
}

/** На столько суток вперёд хватает прогноза погоды. */
private const val DEFAULT_DAYS = 7
