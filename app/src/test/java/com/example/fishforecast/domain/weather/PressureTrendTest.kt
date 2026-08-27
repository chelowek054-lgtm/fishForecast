package com.example.fishforecast.domain.weather

import com.example.fishforecast.data.local.entities.WeatherEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PressureTrendTest {

    private fun hour(index: Int, pressureHpa: Double) = WeatherEntity(
        mapId = 1,
        time = "2026-08-27T%02d:00".format(index),
        temperature = 15.0,
        humidity = 60.0,
        pressure = pressureHpa,
        windSpeed = 10.0,
        weatherCode = 0,
        latitude = 55.0,
        longitude = 37.0
    )

    @Test
    fun `падение отмечается когда давление уходит вниз`() {
        val trend = pressureTrend(listOf(hour(0, 1013.0), hour(6, 1000.0)))!!

        assertEquals(PressureDirection.FALLING, trend.direction)
        assertEquals(-9.8, trend.deltaMmHg, 0.5)
    }

    @Test
    fun `мелкие колебания считаются ровным фоном`() {
        val trend = pressureTrend(listOf(hour(0, 1013.0), hour(6, 1014.0)))!!

        assertEquals(PressureDirection.STEADY, trend.direction)
    }

    @Test
    fun `рост отмечается когда давление идёт вверх`() {
        val trend = pressureTrend(listOf(hour(0, 1000.0), hour(6, 1013.0)))!!

        assertEquals(PressureDirection.RISING, trend.direction)
    }

    @Test
    fun `по одному часу тренда нет`() {
        assertNull(pressureTrend(listOf(hour(0, 1013.0))))
    }
}
