package com.example.fishforecast.domain.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class HourWindowTest {

    private val day = LocalDateTime.of(2026, 8, 28, 0, 0)

    /** Двое суток по часам: с полуночи 28-го. */
    private val hours = (0 until 48).map { day.plusHours(it.toLong()).toString().take(16) }

    private fun windowAt(now: LocalDateTime, back: Int = 12, forward: Int = 24) =
        hourWindow(hours, back, forward, now) { it }

    @Test
    fun `окно берёт часы до и после текущего`() {
        val window = windowAt(LocalDateTime.of(2026, 8, 28, 18, 30))

        // 18:30 — это идущий час 18:00, а не следующий.
        assertEquals(12, window.nowIndex)
        assertEquals("2026-08-28T06:00", window.items.first())
        assertEquals("2026-08-28T18:00", window.items[window.nowIndex])
        assertEquals(37, window.items.size)
    }

    @Test
    fun `в начале ряда окно короче, но отметка не врёт`() {
        // Истории всего три часа — окно просто начинается раньше.
        val window = windowAt(LocalDateTime.of(2026, 8, 28, 3, 10))

        assertEquals(3, window.nowIndex)
        assertEquals("2026-08-28T00:00", window.items.first())
        assertEquals("2026-08-28T03:00", window.items[window.nowIndex])
    }

    @Test
    fun `прошедшие часы отличаются от будущих`() {
        val window = windowAt(LocalDateTime.of(2026, 8, 28, 18, 0))

        assertTrue(window.isPast(0))
        assertFalse(window.isPast(window.nowIndex))
        assertFalse(window.isPast(window.items.lastIndex))
    }

    @Test
    fun `устаревший прогноз показывает свой хвост`() {
        // Все часы позади: выдумывать будущее нельзя, показываем что есть.
        val window = windowAt(LocalDateTime.of(2026, 9, 1, 12, 0))

        assertEquals(window.items.lastIndex, window.nowIndex)
        assertEquals("2026-08-29T23:00", window.items.last())
    }

    @Test
    fun `пустой ряд не ломает окно`() {
        val window = hourWindow(emptyList<String>(), 12, 24, day) { it }

        assertTrue(window.isEmpty)
        assertEquals(-1, window.nowIndex)
        assertFalse(window.isPast(0))
    }

    @Test
    fun `ряд в беспорядке приводится ко времени`() {
        val shuffled = hours.shuffled()

        val window = hourWindow(shuffled, 2, 2, LocalDateTime.of(2026, 8, 28, 10, 0)) { it }

        assertEquals(
            listOf(
                "2026-08-28T08:00",
                "2026-08-28T09:00",
                "2026-08-28T10:00",
                "2026-08-28T11:00",
                "2026-08-28T12:00"
            ),
            window.items
        )
        assertEquals(2, window.nowIndex)
    }

    @Test
    fun `час считается от текущего момента со знаком`() {
        val now = LocalDateTime.of(2026, 8, 28, 12, 0)

        assertEquals(-3L, hoursFromNow("2026-08-28T09:00", now))
        assertEquals(5L, hoursFromNow("2026-08-28T17:00", now))
        assertEquals(null, hoursFromNow("не время", now))
    }
}
