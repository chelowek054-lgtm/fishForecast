package com.example.fishforecast.ui.bite

import kotlin.math.max

/**
 * Разметка оси времени у графика активности.
 *
 * Вынесено из отрисовки намеренно: как выглядит столбик, тестом не проверить,
 * а что подпись стоит под своим часом, что сутки разделены и что подписи не
 * налезают друг на друга — можно. Обе ошибки, которые делали ось нечитаемой,
 * живут именно здесь: раньше подпись стояла у края шестичасовой группы, а
 * потом «сейчас» и дата стали наезжать на соседние часы.
 */
data class HourTick(
    val index: Int,
    /** Подпись под столбиком; пусто — этому часу подписи не досталось. */
    val label: String,
    /** Первый час новых суток: здесь проходит разделитель. */
    val dayStart: Boolean,
    /** Час, на котором стоит рыболов. */
    val now: Boolean
)

/**
 * Размечает ось: какие часы подписаны и где граница суток.
 *
 * Подпись шире своей ячейки — обычное дело: «сейчас» занимает три ячейки, дата
 * две, час одну. Поэтому важная подпись гасит соседние менее важные: лучше
 * показать три подписи, которые читаются, чем семь, которые слиплись.
 *
 * [cellChars] — сколько символов помещается в ширину одной ячейки. Число
 * приблизительное и приходит из вёрстки: точную ширину текста здесь измерить
 * нечем, а промах в одну ячейку стоит лишь пропущенной подписи.
 */
fun chartTicks(
    times: List<String>,
    nowIndex: Int,
    stepHours: Int = LABEL_STEP_HOURS,
    cellChars: Int = CELL_CHARS
): List<HourTick> {
    val candidates = times.mapIndexed { index, time ->
        // Первый столбик началом суток не считается: слева от него ничего нет,
        // и разделитель повис бы на краю графика. Неразобранный сосед — тоже
        // не повод: про границу суток тогда ничего не известно, и выдумывать
        // её хуже, чем промолчать.
        val date = dateOf(time)
        val previousDate = times.getOrNull(index - 1)?.let { dateOf(it) }
        val dayStart = index > 0 && date != null && previousDate != null && date != previousDate
        val now = index == nowIndex
        val hour = hourOf(time)

        val label = when {
            now -> NOW_LABEL
            dayStart -> dayLabel(time)
            hour != null && stepHours > 0 && hour % stepHours == 0 -> "%02d".format(hour)
            else -> ""
        }
        // «Сейчас» важнее даты, дата важнее часа: когда они сходятся в одном
        // месте, остаться должна та подпись, ради которой на ось и смотрят.
        val priority = when {
            now -> 3
            dayStart -> 2
            label.isNotEmpty() -> 1
            else -> 0
        }

        HourTick(index = index, label = label, dayStart = dayStart, now = now) to priority
    }

    val kept = keepReadable(candidates, cellChars)
    return candidates.mapIndexed { index, pair ->
        if (index in kept) pair.first else pair.first.copy(label = "")
    }
}

/**
 * Оставляет те подписи, которые не наезжают друг на друга.
 *
 * Разбор идёт от важных к второстепенным: занятое место больше не отдаётся.
 */
private fun keepReadable(
    candidates: List<Pair<HourTick, Int>>,
    cellChars: Int
): Set<Int> {
    val kept = mutableSetOf<Int>()
    val busy = mutableListOf<IntRange>()

    candidates
        .filter { it.second > 0 }
        .sortedWith(compareByDescending<Pair<HourTick, Int>> { it.second }.thenBy { it.first.index })
        .forEach { (tick, _) ->
            val range = occupiedCells(tick.index, tick.label, cellChars)
            if (busy.none { it.first <= range.last && range.first <= it.last }) {
                kept += tick.index
                busy += range
            }
        }

    return kept
}

/**
 * Сколько ячеек занимает подпись. Она стоит по центру своей ячейки, поэтому
 * лишнее уходит в обе стороны поровну.
 */
internal fun occupiedCells(index: Int, label: String, cellChars: Int): IntRange {
    val perCell = max(1, cellChars)
    val cells = (label.length + perCell - 1) / perCell
    val spill = cells / 2
    return (index - spill)..(index + spill)
}

/**
 * Час из времени прогноза. Формат задаёт Open-Meteo: `2026-08-29T16:00`.
 * Непонятную строку разбираем в null — график должен рисоваться и без подписи.
 */
internal fun hourOf(time: String): Int? =
    time.substringAfter('T', "").take(2).toIntOrNull()?.takeIf { it in 0..23 }

/** Дата без времени: по её смене и находится граница суток. */
internal fun dateOf(time: String): String? =
    time.substringBefore('T', "").takeIf { it.length == 10 }

/** Подпись дня по-человечески: «29.08». */
internal fun dayLabel(time: String): String {
    val date = dateOf(time) ?: return ""
    return "${date.substring(8, 10)}.${date.substring(5, 7)}"
}

/** Через сколько часов ставится подпись на оси. */
const val LABEL_STEP_HOURS = 3

/** Подпись текущего часа. Вынесена: по ней же считается её ширина. */
const val NOW_LABEL = "сейчас"

/** Сколько символов помещается в ширину ячейки часа. */
const val CELL_CHARS = 3
