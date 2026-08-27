package com.example.fishforecast.domain.water

import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import java.time.LocalDateTime

/**
 * Ход воды в двух слоях района.
 *
 * Две кривые вместо одной — весь смысл расчёта: разница между мелью и ямой
 * и говорит, где сегодня рыба. Одна усреднённая температура водоёма такой
 * подсказки не даёт.
 */
data class WaterState(
    val shallow: List<WaterHour>,
    val deep: List<WaterHour>,
    val shallowDepthM: Double,
    val deepDepthM: Double,
    /** Глубины не заданы рыболовом — значит, взяты типовые. */
    val depthsAssumed: Boolean,
    /** Расчёт опирается на замер термометром, а не только на погоду. */
    val anchored: Boolean
) {
    val isEmpty: Boolean get() = shallow.isEmpty()

    /** Слой на заданный час; null, пока расчёт не дошёл до этого времени. */
    fun shallowAt(time: String): Double? = shallow.firstOrNull { it.time == time }?.temperature

    fun deepAt(time: String): Double? = deep.firstOrNull { it.time == time }?.temperature
}

/**
 * Считает воду для карты. Глубины берёт у рыболова, а если он их не задал —
 * типовые для небольшого водоёма, и честно об этом сообщает.
 */
fun calculateWaterState(forecast: List<WeatherEntity>, map: SavedMapEntity?): WaterState {
    val shallowLayer = map?.shallowDepthM?.let { WaterLayer(it) } ?: DEFAULT_SHALLOW
    val deepLayer = map?.deepDepthM?.let { WaterLayer(it) } ?: DEFAULT_DEEP

    val anchor = map?.let { saved ->
        val temperature = saved.waterTempC
        val time = saved.waterTempAt
        if (temperature != null && time != null) WaterMeasurement(time, temperature) else null
    }

    return WaterState(
        shallow = simulateWaterTemperature(forecast, shallowLayer, anchor),
        deep = simulateWaterTemperature(forecast, deepLayer, anchor),
        shallowDepthM = shallowLayer.depthM,
        deepDepthM = deepLayer.depthM,
        depthsAssumed = map?.shallowDepthM == null || map.deepDepthM == null,
        anchored = anchor != null
    )
}

/**
 * Куда идёт вода за ближайшие часы. Остывание — то самое, ради чего всё
 * считается: вместе с ним в воду приходит кислород.
 */
fun waterTrend(hours: List<WaterHour>, windowHours: Int = 6): Double? {
    if (hours.size < 2) return null
    val window = hours.take(windowHours + 1)
    return window.last().temperature - window.first().temperature
}

/** Часы, оставшиеся от текущего момента и дальше. */
fun List<WaterHour>.fromNow(now: LocalDateTime = LocalDateTime.now()): List<WaterHour> =
    dropWhile { LocalDateTime.parse(it.time).isBefore(now.minusHours(1)) }
