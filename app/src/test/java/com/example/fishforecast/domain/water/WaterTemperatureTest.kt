package com.example.fishforecast.domain.water

import com.example.fishforecast.data.local.entities.WeatherEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class WaterTemperatureTest {

    /** Сутки погоды с суточным ходом температуры и солнца. */
    private fun day(
        start: LocalDateTime,
        meanAir: Double,
        humidity: Double = 70.0,
        windKmh: Double = 10.0,
        peakRadiation: Double = 600.0
    ): List<WeatherEntity> = (0 until 24).map { hour ->
        val daylight = if (hour in 6..18) {
            kotlin.math.sin((hour - 6) / 12.0 * Math.PI)
        } else {
            0.0
        }
        WeatherEntity(
            mapId = 1,
            time = start.plusHours(hour.toLong()).toString().take(16),
            temperature = meanAir + 5 * daylight - 2.5,
            humidity = humidity,
            pressure = 1005.0,
            windSpeed = windKmh,
            shortwaveRadiation = peakRadiation * daylight,
            weatherCode = 0,
            latitude = 55.0,
            longitude = 37.0
        )
    }

    /** @param fromDay смещение от начала ряда: часы не должны накладываться. */
    private fun days(
        count: Int,
        meanAir: Double,
        windKmh: Double = 10.0,
        fromDay: Int = 0
    ): List<WeatherEntity> {
        val start = LocalDateTime.of(2026, 8, 20, 0, 0).plusDays(fromDay.toLong())
        return (0 until count).flatMap { day(start.plusDays(it.toLong()), meanAir, windKmh = windKmh) }
    }

    @Test
    fun `мель отзывается на похолодание быстрее ямы`() {
        // Неделя тепла, затем резкое похолодание — ситуация из разбора.
        val warm = days(7, meanAir = 28.0)
        val hours = warm + days(2, meanAir = 12.0, fromDay = 7)

        val shallow = simulateWaterTemperature(hours, WaterLayer(1.0))
        val deep = simulateWaterTemperature(hours, WaterLayer(6.0))

        val shallowDrop = shallow[warm.lastIndex].temperature - shallow.last().temperature
        val deepDrop = deep[warm.lastIndex].temperature - deep.last().temperature

        assertTrue("Мель должна терять больше: мель $shallowDrop, яма $deepDrop", shallowDrop > deepDrop)
        assertTrue("Обе должны остывать", deepDrop > 0)
        // Ради этой картины всё и считается: за трое суток похолодания мель
        // теряет около девяти градусов, яма — меньше трёх.
        assertTrue("Мель должна терять заметно, а не на десятые", shallowDrop > 3.0)
    }

    @Test
    fun `вода тянется за погодой, но не повторяет её`() {
        val hours = days(10, meanAir = 20.0)
        val water = simulateWaterTemperature(hours, WaterLayer(2.0)).map { it.temperature }

        val airSwing = hours.takeLast(24).let { it.maxOf { h -> h.temperature } - it.minOf { h -> h.temperature } }
        val waterSwing = water.takeLast(24).let { it.max() - it.min() }

        assertTrue("Суточный размах воды должен быть меньше воздушного", waterSwing < airSwing)
        assertTrue("Вода за десять дней должна прийти к разумным значениям", water.last() in 10.0..30.0)
    }

    @Test
    fun `замер термометром переставляет модель на факт`() {
        val hours = days(3, meanAir = 22.0)
        val anchorTime = hours[24].time

        val measured = simulateWaterTemperature(
            hours = hours,
            layer = WaterLayer(2.0),
            anchor = WaterMeasurement(time = anchorTime, temperature = 15.0)
        )
        val free = simulateWaterTemperature(hours, WaterLayer(2.0))

        assertTrue(
            "После замера модель должна идти от него, а не от догадки",
            measured[24].temperature < free[24].temperature
        )
    }

    @Test
    fun `ветер ускоряет остывание`() {
        val calm = days(4, meanAir = 26.0, windKmh = 2.0) +
            days(2, meanAir = 10.0, windKmh = 2.0, fromDay = 4)
        val windy = days(4, meanAir = 26.0, windKmh = 40.0) +
            days(2, meanAir = 10.0, windKmh = 40.0, fromDay = 4)

        val calmWater = simulateWaterTemperature(calm, WaterLayer(2.0)).last().temperature
        val windyWater = simulateWaterTemperature(windy, WaterLayer(2.0)).last().temperature

        assertTrue("В ветер вода остывает быстрее", windyWater < calmWater)
    }

    @Test
    fun `пустой прогноз не ломает расчёт`() {
        assertEquals(emptyList<WaterHour>(), simulateWaterTemperature(emptyList(), WaterLayer(2.0)))
    }

    @Test
    fun `точка росы ниже температуры воздуха и падает с влажностью`() {
        assertEquals(20.0, dewPoint(20.0, 100.0), 0.3)
        assertTrue(dewPoint(20.0, 50.0) < dewPoint(20.0, 80.0))
    }
}
