package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateFishActivityUseCaseTest {

    private val useCase = CalculateFishActivityUseCase()

    /** Щука из базового справочника: 8–18 °C, 740–760 мм рт. ст. */
    private val pike = FishEntity(
        id = 1,
        name = "Щука",
        description = "",
        minTemp = 8f,
        maxTemp = 18f,
        minPressure = 740f,
        maxPressure = 760f,
        moonPhaseImpact = "None"
    )

    /** 750 мм рт. ст. в гПа — единицы у справочника и погоды разные. */
    private val comfortablePressureHpa = 750.0 * 1.333224

    private fun hour(
        index: Int,
        temperature: Double = 13.0,
        pressureHpa: Double = comfortablePressureHpa,
        windSpeed: Double = 5.0
    ) = WeatherEntity(
        time = "2026-08-26T%02d:00".format(index),
        temperature = temperature,
        humidity = 60.0,
        pressure = pressureHpa,
        windSpeed = windSpeed,
        weatherCode = 0,
        latitude = 55.0,
        longitude = 37.0
    )

    @Test
    fun `идеальные условия дают высокую оценку`() {
        val forecast = (0..5).map { hour(it) }

        val result = useCase(pike, forecast)

        assertEquals(BiteLevel.GOOD, result.last().level)
        assertTrue("ожидался высокий score, получен ${result.last().score}", result.last().score >= 90)
    }

    @Test
    fun `холод за пределами комфорта снижает оценку`() {
        val comfortable = useCase(pike, (0..5).map { hour(it, temperature = 13.0) }).last().score
        val cold = useCase(pike, (0..5).map { hour(it, temperature = 2.0) }).last().score

        assertTrue("холод должен снижать оценку: $cold vs $comfortable", cold < comfortable)
    }

    @Test
    fun `давление вне привычного диапазона снижает оценку`() {
        val normal = useCase(pike, (0..5).map { hour(it) }).last().score
        val lowPressure = useCase(
            pike,
            (0..5).map { hour(it, pressureHpa = 720.0 * 1.333224) }
        ).last().score

        assertTrue("низкое давление должно снижать оценку: $lowPressure vs $normal", lowPressure < normal)
    }

    @Test
    fun `резкий скачок давления за три часа снижает оценку`() {
        // Первые часы — 750 мм, дальше резкий рост до 758 мм.
        val forecast = listOf(
            hour(0),
            hour(1),
            hour(2),
            hour(3, pressureHpa = 758.0 * 1.333224),
            hour(4, pressureHpa = 758.0 * 1.333224)
        )

        val result = useCase(pike, forecast)
        val jumpHour = result[3]

        assertTrue(jumpHour.score < result[2].score)
        assertTrue(
            jumpHour.factors.first { it.name == "Тенденция" }.comment.contains("Растёт")
        )
    }

    @Test
    fun `первые часы не штрафуются за отсутствие истории`() {
        val result = useCase(pike, (0..2).map { hour(it) })

        result.forEach { forecast ->
            val trend = forecast.factors.first { it.name == "Тенденция" }
            assertEquals(1.0, trend.value, 0.0001)
            assertTrue(trend.comment.contains("Недостаточно истории"))
        }
    }

    @Test
    fun `сильный ветер ухудшает оценку`() {
        val calm = useCase(pike, (0..5).map { hour(it, windSpeed = 5.0) }).last().score
        val storm = useCase(pike, (0..5).map { hour(it, windSpeed = 45.0) }).last().score

        assertTrue("сильный ветер должен снижать оценку: $storm vs $calm", storm < calm)
    }

    @Test
    fun `оценка не выходит за границы ноль сто`() {
        val awful = useCase(
            pike,
            (0..5).map { hour(it, temperature = -30.0, pressureHpa = 700.0 * 1.333224, windSpeed = 90.0) }
        )

        awful.forEach {
            assertTrue(it.score in 0..100)
        }
        assertEquals(BiteLevel.POOR, awful.last().level)
    }

    @Test
    fun `пустой прогноз даёт пустой результат`() {
        assertTrue(useCase(pike, emptyList()).isEmpty())
    }

    @Test
    fun `часы сортируются по времени независимо от порядка входа`() {
        val shuffled = listOf(hour(3), hour(1), hour(0), hour(2))

        val result = useCase(pike, shuffled)

        assertEquals(listOf("2026-08-26T00:00", "2026-08-26T01:00", "2026-08-26T02:00", "2026-08-26T03:00"),
            result.map { it.time })
    }
}
