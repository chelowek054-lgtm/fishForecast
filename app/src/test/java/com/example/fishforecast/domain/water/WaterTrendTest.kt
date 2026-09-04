package com.example.fishforecast.domain.water

import com.example.fishforecast.data.local.entities.WeatherEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Ход воды на ближайшие часы.
 *
 * Главное здесь — граница света. Шесть часов подряд, взятые через рассвет,
 * складывали ночное остывание с утренним прогревом, и в три часа ночи
 * приложение писало «мель прогревается». Про сумму это была правда, про
 * текущий момент — нет.
 */
class WaterTrendTest {

    private val start = LocalDateTime.parse("2026-09-04T00:00")

    private fun timeOf(index: Int) = start.plusHours(index.toLong()).toString()

    /** Сутки, где солнце встаёт в 06:00 и садится в 20:00. */
    private fun weather(hours: Int = 24) = (0 until hours).map { index ->
        val hourOfDay = start.plusHours(index.toLong()).hour
        WeatherEntity(
            mapId = 1,
            time = timeOf(index),
            temperature = 18.0,
            humidity = 70.0,
            pressure = 1010.0,
            windSpeed = 5.0,
            windDirection = 200.0,
            weatherCode = 0,
            shortwaveRadiation = if (hourOfDay in 6..19) 400.0 else 0.0,
            latitude = 55.0,
            longitude = 37.0
        )
    }

    /** Вода: ночью падает на 0.1° в час, днём растёт на 0.4°. */
    private fun water(hours: Int = 24): List<WaterHour> {
        var temperature = 20.0
        return (0 until hours).map { index ->
            val hourOfDay = start.plusHours(index.toLong()).hour
            if (index > 0) {
                temperature += if (hourOfDay in 6..19) 0.4 else -0.1
            }
            WaterHour(time = timeOf(index), temperature = temperature)
        }
    }

    @Test
    fun `ночное окно обрывается на рассвете`() {
        // Три часа ночи: целиком тёмных часов впереди только два — в 06:00
        // солнце уже светит, и этот час к ночи не относится.
        val trend = waterTrend(water().drop(3), weather())

        assertTrue("должна быть ночь", trend!!.dark)
        assertEquals(2, trend.hours)
        assertEquals(-0.2, trend.deltaC, 0.001)
    }

    @Test
    fun `ночью вода не может прогреваться чужим дневным теплом`() {
        // Именно этот случай и читался как «мель прогревается» в три ночи:
        // шесть часов от 03:00 включали три часа солнца.
        val trend = waterTrend(water().drop(3), weather())

        assertTrue("ход должен остаться отрицательным: $trend", trend!!.deltaC < 0)
    }

    @Test
    fun `днём окно берётся целиком`() {
        // Полдень: до заката ещё далеко, ограничение — сами шесть часов.
        val trend = waterTrend(water().drop(12), weather())

        assertEquals(false, trend!!.dark)
        assertEquals(6, trend.hours)
        assertEquals(2.4, trend.deltaC, 0.001)
    }

    @Test
    fun `последний час перед сменой света не даёт хода`() {
        // 05:00 — дальше сразу рассвет. Одна точка ходом не является, и
        // лучше промолчать, чем выдать за ночь первый час утра.
        assertNull(waterTrend(water().drop(5), weather()))
    }

    @Test
    fun `без погоды на этот час хода нет`() {
        // Расчёт воды уходит дальше прогноза; врать про свет мы не будем.
        assertNull(waterTrend(water(), weather(hours = 0)))
    }
}
