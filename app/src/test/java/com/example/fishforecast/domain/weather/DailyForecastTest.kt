package com.example.fishforecast.domain.weather

import com.example.fishforecast.data.local.entities.WeatherEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyForecastTest {

    private fun hour(
        time: String,
        temperature: Double = 15.0,
        pressureHpa: Double = 1013.0,
        windSpeed: Double = 10.0,
        windDirection: Double = 0.0,
        weatherCode: Int = 0,
        precipitationChance: Double = 0.0
    ) = WeatherEntity(
        mapId = 1,
        time = time,
        temperature = temperature,
        humidity = 60.0,
        pressure = pressureHpa,
        windSpeed = windSpeed,
        windDirection = windDirection,
        precipitationChance = precipitationChance,
        weatherCode = weatherCode,
        latitude = 55.0,
        longitude = 37.0
    )

    @Test
    fun `день и ночь берутся из своих часов`() {
        val hours = listOf(
            hour("2026-08-27T03:00", temperature = 8.0),
            hour("2026-08-27T14:00", temperature = 25.0),
            hour("2026-08-27T23:00", temperature = 11.0)
        )

        val day = hours.toDailyForecast().single()

        assertEquals(25.0, day.dayTemperature, 0.001)
        assertEquals(8.0, day.nightTemperature, 0.001)
    }

    @Test
    fun `погоду дня определяет самый тяжёлый час`() {
        val hours = listOf(
            hour("2026-08-27T10:00", weatherCode = 1),
            hour("2026-08-27T12:00", weatherCode = 95),
            hour("2026-08-27T15:00", weatherCode = 2)
        )

        assertEquals(95, hours.toDailyForecast().single().weatherCode)
    }

    @Test
    fun `часы разных суток расходятся по дням в хронологическом порядке`() {
        val hours = listOf(
            hour("2026-08-28T10:00"),
            hour("2026-08-27T10:00")
        )

        val days = hours.toDailyForecast()

        assertEquals(2, days.size)
        assertEquals("2026-08-27", days.first().date.toString())
    }

    @Test
    fun `сутки без дневных часов опираются на то что есть`() {
        val hours = listOf(hour("2026-08-27T23:00", temperature = 7.0))

        val day = hours.toDailyForecast().single()

        assertEquals(7.0, day.dayTemperature, 0.001)
        assertEquals(7.0, day.nightTemperature, 0.001)
    }

    @Test
    fun `давление дня переводится в миллиметры`() {
        val hours = listOf(hour("2026-08-27T10:00", pressureHpa = 1013.25))

        assertEquals(760.0, hours.toDailyForecast().single().pressureMmHg, 0.5)
    }

    @Test
    fun `вероятность осадков берётся по максимуму за день`() {
        val hours = listOf(
            hour("2026-08-27T10:00", precipitationChance = 10.0),
            hour("2026-08-27T16:00", precipitationChance = 70.0)
        )

        assertEquals(70, hours.toDailyForecast().single().precipitationChance)
    }
}
