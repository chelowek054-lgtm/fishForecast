package com.example.fishforecast.domain.bite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Свёртка недели по частям суток.
 *
 * Почасовой ряд отвечает «ехать ли сегодня», эта таблица — «когда брать
 * отгул». Поэтому здесь проверяется не арифметика ради арифметики, а то, что
 * планировать по ней можно: прошедшее не показывается, неполные части не
 * притворяются полными, а пик виден отдельно от среднего.
 */
class WeekActivityTest {

    private val start = LocalDateTime.parse("2026-09-07T00:00")

    /** Ряд часов подряд; балл задаётся функцией от часа с начала ряда. */
    private fun forecast(hours: Int, score: (Int) -> Int): List<BiteForecast> =
        (0 until hours).map { index ->
            val value = score(index)
            BiteForecast(
                time = start.plusHours(index.toLong()).toString(),
                score = value,
                level = BiteLevel.fromScore(value),
                factors = emptyList()
            )
        }

    @Test
    fun `сутки делятся на четыре части`() {
        val week = weekActivity(forecast(24) { 50 }, from = start)

        assertEquals(1, week.size)
        assertEquals(
            listOf(PartOfDay.NIGHT, PartOfDay.MORNING, PartOfDay.DAY, PartOfDay.EVENING),
            week.first().parts.map { it.part }
        )
    }

    @Test
    fun `в клетке средний балл части, а не лучший час`() {
        // Утро: пять часов по 40 и один час 100. Среднее — 50, пик — 100.
        val week = weekActivity(
            forecast(24) { hour -> if (hour == 9) 100 else 40 },
            from = start
        )
        val morning = week.first().parts.first { it.part == PartOfDay.MORNING }

        assertEquals(50, morning.score)
        assertEquals(100, morning.bestScore)
        assertEquals("09:00", morning.bestHour)
    }

    @Test
    fun `прошедшие части не показываются`() {
        // Планируем с полудня: ночь и утро этих суток уже позади.
        val week = weekActivity(forecast(24) { 60 }, from = start.withHour(12))

        assertEquals(
            listOf(PartOfDay.DAY, PartOfDay.EVENING),
            week.first().parts.map { it.part }
        )
    }

    @Test
    fun `неполная часть честно сообщает, сколько в ней часов`() {
        // Планируем с 15:00: от дневной части остались три часа.
        val week = weekActivity(forecast(24) { 60 }, from = start.withHour(15))
        val day = week.first().parts.first { it.part == PartOfDay.DAY }

        assertEquals(3, day.hours)
    }

    @Test
    fun `неделя обрывается там, где кончается прогноз`() {
        // Семь суток данных — семь строк, ни одной выдуманной.
        val week = weekActivity(forecast(24 * 7) { 55 }, from = start)

        assertEquals(7, week.size)
        assertEquals(LocalDate.parse("2026-09-13"), week.last().date)
    }

    @Test
    fun `дальше недели не заглядываем`() {
        val week = weekActivity(forecast(24 * 12) { 55 }, from = start)

        assertEquals(7, week.size)
    }

    @Test
    fun `лучшая часть дня — та, где средний балл выше`() {
        val week = weekActivity(
            forecast(24) { hour -> if (PartOfDay.of(hour % 24) == PartOfDay.EVENING) 90 else 30 },
            from = start
        )

        assertEquals(PartOfDay.EVENING, week.first().best?.part)
        assertEquals(90, week.first().best?.score)
    }

    @Test
    fun `без прогноза таблицы нет`() {
        assertTrue(weekActivity(emptyList(), from = start).isEmpty())
    }

    @Test
    fun `когда весь прогноз в прошлом, таблица пустая`() {
        // Не строка с нулями: нулей там нет, там просто нечего показывать.
        assertTrue(weekActivity(forecast(24) { 70 }, from = start.plusDays(3)).isEmpty())
    }

    @Test
    fun `непонятное время часа не роняет свёртку`() {
        val broken = listOf(
            BiteForecast(time = "не время", score = 90, level = BiteLevel.GOOD, factors = emptyList())
        ) + forecast(6) { 40 }

        val week = weekActivity(broken, from = start)

        assertEquals(1, week.size)
        assertEquals(40, week.first().parts.single().score)
    }

    @Test
    fun `границы частей суток не пересекаются и покрывают сутки`() {
        val covered = (0 until 24).map { PartOfDay.of(it) }

        assertEquals(6, covered.count { it == PartOfDay.NIGHT })
        assertEquals(6, covered.count { it == PartOfDay.MORNING })
        assertEquals(6, covered.count { it == PartOfDay.DAY })
        assertEquals(6, covered.count { it == PartOfDay.EVENING })
        assertNull(covered.firstOrNull { it.fromHour >= it.untilHour })
    }
}
