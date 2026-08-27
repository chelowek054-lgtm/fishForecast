package com.example.fishforecast.domain.weather

import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Сводка одного дня, собранная из почасового прогноза.
 *
 * Open-Meteo отдаёт часы, но на неделю вперёд рыболову нужны не 168 строк,
 * а колонка на день: что за погода, насколько тепло днём и ночью, куда
 * и как сильно дует, что с давлением.
 */
data class DailyForecast(
    val date: LocalDate,
    /** Дневной максимум — то, что подписывают крупной цифрой. */
    val dayTemperature: Double,
    /** Ночной минимум: клёв часто решается именно им. */
    val nightTemperature: Double,
    val weatherCode: Int,
    /** Максимум вероятности осадков за день, %. */
    val precipitationChance: Int,
    val pressureMmHg: Double,
    val windSpeedKmh: Double,
    val windDirection: Double
) {
    val sky: Sky get() = skyOf(weatherCode)
}

/** Часы дня, которые считаются дневными: остальное уходит в ночную кривую. */
private val DAY_HOURS = 9..20

fun List<WeatherEntity>.toDailyForecast(): List<DailyForecast> =
    groupBy { LocalDateTime.parse(it.time).toLocalDate() }
        .toSortedMap()
        .map { (date, hours) -> hours.summarize(date) }

private fun List<WeatherEntity>.summarize(date: LocalDate): DailyForecast {
    val dayHours = filter { LocalDateTime.parse(it.time).hour in DAY_HOURS }
    val nightHours = filter { LocalDateTime.parse(it.time).hour !in DAY_HOURS }

    // Если день начался с полудня (сегодня) или обрывается вечером, брать
    // нечего — тогда обе кривые опираются на то, что есть.
    val dayTemperature = (dayHours.ifEmpty { this }).maxOf { it.temperature }
    val nightTemperature = (nightHours.ifEmpty { this }).minOf { it.temperature }

    // Погоду дня определяет самый тяжёлый час: два часа грозы важнее
    // двадцати часов переменной облачности.
    val weatherCode = maxOf { weatherSeverity(it.weatherCode) }
        .let { severity -> first { weatherSeverity(it.weatherCode) == severity }.weatherCode }

    // Ветер показываем дневной: ночью на воде обычно никого нет.
    val windReference = (dayHours.ifEmpty { this }).maxBy { it.windSpeed }

    return DailyForecast(
        date = date,
        dayTemperature = dayTemperature,
        nightTemperature = nightTemperature,
        weatherCode = weatherCode,
        precipitationChance = maxOf { it.precipitationChance }.toInt(),
        pressureMmHg = map { it.pressure }.average().hPaToMmHg(),
        windSpeedKmh = windReference.windSpeed,
        windDirection = windReference.windDirection
    )
}
