package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class FindBiteWindowUseCaseTest {

    private val useCase = FindBiteWindowUseCase(CalculateFishActivityUseCase())

    private val pike = FishEntity(
        id = 1, name = "Щука", description = "",
        minTemp = 8f, maxTemp = 18f,
        minPressure = 740f, maxPressure = 760f,
        moonPhaseImpact = "None"
    )

    /** Карп любит теплее: в холодной воде его окно не откроется. */
    private val carp = FishEntity(
        id = 2, name = "Карп", description = "",
        minTemp = 20f, maxTemp = 28f,
        minPressure = 740f, maxPressure = 750f,
        moonPhaseImpact = "None"
    )

    private val comfortablePressureHpa = 750.0 * 1.333224

    private fun hour(index: Int, temperature: Double = 13.0) = WeatherEntity(
        time = "2026-08-26T%02d:00".format(index),
        temperature = temperature,
        humidity = 60.0,
        pressure = comfortablePressureHpa,
        windSpeed = 5.0,
        weatherCode = 0,
        latitude = 55.0,
        longitude = 37.0
    )

    private val startOfDay: LocalDateTime = LocalDateTime.parse("2026-08-26T00:00")

    @Test
    fun `находит час с хорошим клёвом`() {
        val window = useCase(listOf(pike), (0..12).map { hour(it) }, from = startOfDay)

        assertTrue(window != null)
        assertEquals("Щука", window!!.fish.name)
        assertTrue(window.forecast.score >= 75)
    }

    @Test
    fun `прошедшие часы не предлагаются`() {
        val forecast = (0..12).map { hour(it) }

        val window = useCase(listOf(pike), forecast, from = LocalDateTime.parse("2026-08-26T10:00"))

        assertTrue(window != null)
        assertTrue(
            "предложен прошедший час ${window!!.forecast.time}",
            LocalDateTime.parse(window.forecast.time).isAfter(LocalDateTime.parse("2026-08-26T10:00"))
        )
    }

    @Test
    fun `за горизонтом ожидания ничего не ищем`() {
        val forecast = (0..12).map { hour(it) }

        val window = useCase(
            fishList = listOf(pike),
            forecast = forecast,
            from = startOfDay,
            lookaheadHours = 2
        )

        assertTrue(window != null)
        assertTrue(!LocalDateTime.parse(window!!.forecast.time).isAfter(startOfDay.plusHours(2)))
    }

    @Test
    fun `когда условия плохи окна нет`() {
        val icy = (0..12).map { hour(it, temperature = -20.0) }

        assertNull(useCase(listOf(pike), icy, from = startOfDay))
    }

    @Test
    fun `из нескольких рыб выбирается лучшая`() {
        // 13 °C — комфорт для щуки и холодно для карпа.
        val window = useCase(listOf(carp, pike), (0..12).map { hour(it) }, from = startOfDay)

        assertEquals("Щука", window?.fish?.name)
    }

    @Test
    fun `пустой справочник или прогноз не дают окна`() {
        assertNull(useCase(emptyList(), (0..5).map { hour(it) }, from = startOfDay))
        assertNull(useCase(listOf(pike), emptyList(), from = startOfDay))
    }

    @Test
    fun `битое время в прогнозе не роняет поиск`() {
        val broken = listOf(hour(0).copy(time = "не время"), hour(1), hour(2), hour(3))

        val window = useCase(listOf(pike), broken, from = startOfDay)

        assertTrue(window == null || LocalDateTime.parse(window.forecast.time) != null)
    }
}
