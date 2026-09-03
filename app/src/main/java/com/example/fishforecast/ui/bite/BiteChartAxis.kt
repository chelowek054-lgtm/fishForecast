package com.example.fishforecast.ui.bite

/**
 * Разметка оси времени у графика активности.
 *
 * Вынесено из отрисовки намеренно: как выглядит столбик, тестом не проверить,
 * а что подпись стоит под своим часом, что сутки разделены и что «сейчас» не
 * потерялось — можно. Ошибка именно здесь и делала график нечитаемым: подпись
 * стояла у края шестичасовой группы и отвечала на вопрос «когда началась
 * группа», а не «который час у этого столбика».
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
 * Приоритет подписей: «сейчас» важнее даты, дата важнее часа. Иначе в одном
 * месте столкнулись бы три подписи и не осталось бы ни одной читаемой.
 */
fun chartTicks(
    times: List<String>,
    nowIndex: Int,
    stepHours: Int = LABEL_STEP_HOURS
): List<HourTick> = times.mapIndexed { index, time ->
    // Первый столбик началом суток не считается: слева от него ничего нет,
    // и разделитель повис бы на краю графика. Неразобранный сосед — тоже не
    // повод: про границу суток тогда ничего не известно, и выдумывать её
    // хуже, чем промолчать.
    val date = dateOf(time)
    val previousDate = times.getOrNull(index - 1)?.let { dateOf(it) }
    val dayStart = index > 0 && date != null && previousDate != null && date != previousDate
    val now = index == nowIndex
    val hour = hourOf(time)

    HourTick(
        index = index,
        label = when {
            now -> "сейчас"
            dayStart -> dayLabel(time)
            hour != null && stepHours > 0 && hour % stepHours == 0 -> "%02d".format(hour)
            else -> ""
        },
        dayStart = dayStart,
        now = now
    )
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
