package com.example.fishforecast.ui.bite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiteChartAxisTest {

    /** Сутки часами с 12:00 первого дня: как раз перевалит через полночь. */
    private fun hours(count: Int, from: Int = 12, day: Int = 29): List<String> =
        (0 until count).map { step ->
            val hour = (from + step) % 24
            val date = day + (from + step) / 24
            "2026-08-%02dT%02d:00".format(date, hour)
        }

    @Test
    fun `подпись стоит под своим часом, а не у края группы`() {
        val ticks = chartTicks(hours(12), nowIndex = -1)

        // 12:00 — третий час, значит подписан; 13:00 и 14:00 — нет.
        assertEquals("12", ticks[0].label)
        assertEquals("", ticks[1].label)
        assertEquals("", ticks[2].label)
        assertEquals("15", ticks[3].label)
        assertEquals(3, ticks[3].index)
    }

    @Test
    fun `смена суток отмечена и подписана датой`() {
        val ticks = chartTicks(hours(count = 16, from = 20, day = 29), nowIndex = -1)

        val boundary = ticks.first { it.dayStart }
        assertEquals(4, boundary.index)
        assertEquals("30.08", boundary.label)
        assertEquals(1, ticks.count { it.dayStart })
    }

    @Test
    fun `первый столбик началом суток не считается`() {
        // Слева от него ничего нет: разделитель повис бы на краю графика.
        val ticks = chartTicks(listOf("2026-08-29T00:00", "2026-08-29T01:00"), nowIndex = -1)

        assertFalse(ticks.first().dayStart)
    }

    @Test
    fun `час рыболова подписан словом и важнее даты`() {
        val times = hours(count = 8, from = 22, day = 29)
        // Индекс 2 — ровно полночь, то есть и начало суток тоже.
        val ticks = chartTicks(times, nowIndex = 2)

        assertTrue(ticks[2].now)
        assertTrue("разделитель суток остаётся", ticks[2].dayStart)
        assertEquals("сейчас", ticks[2].label)
        assertEquals(1, ticks.count { it.now })
    }

    @Test
    fun `непонятное время не роняет разметку`() {
        // Строка из чужого источника не должна оставлять экран без графика.
        val ticks = chartTicks(listOf("не время", "2026-08-29T09:00"), nowIndex = -1)

        assertEquals("", ticks[0].label)
        assertFalse(ticks[0].dayStart)
        assertEquals("09", ticks[1].label)
        assertNull(hourOf("не время"))
        assertNull(dateOf("2026-08"))
    }

    @Test
    fun `шаг подписей задаётся снаружи`() {
        val ticks = chartTicks(hours(count = 7, from = 0), nowIndex = -1, stepHours = 6)

        assertEquals(listOf("00", "", "", "", "", "", "06"), ticks.map { it.label })
    }
}
