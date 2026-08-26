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
        mapId = 1,
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
    fun `уход давления от нормы снижает оценку`() {
        // Норма водоёма 750, давление уходит вверх до 758.
        val forecast = listOf(
            hour(0), hour(1), hour(2),
            hour(3, pressureHpa = 758.0 * 1.333224),
            hour(4, pressureHpa = 758.0 * 1.333224)
        )

        val result = useCase(pike, forecast, normalPressureMmHg = 750.0)
        val jumpHour = result[3]

        assertTrue(jumpHour.score < result[2].score)
        assertTrue(
            jumpHour.factors.first { it.name == "Тенденция" }.comment.contains("Уходит вверх")
        )
    }

    @Test
    fun `возврат к норме оценивается лучше ухода от неё`() {
        // Одинаковый по величине скачок в 6 мм: сначала к норме, потом от неё.
        val toNormal = listOf(
            hour(0, pressureHpa = 756.0 * 1.333224),
            hour(1, pressureHpa = 756.0 * 1.333224),
            hour(2, pressureHpa = 756.0 * 1.333224),
            hour(3, pressureHpa = 750.0 * 1.333224)
        )
        val fromNormal = listOf(
            hour(0, pressureHpa = 750.0 * 1.333224),
            hour(1, pressureHpa = 750.0 * 1.333224),
            hour(2, pressureHpa = 750.0 * 1.333224),
            hour(3, pressureHpa = 744.0 * 1.333224)
        )

        val returning = useCase(pike, toNormal, normalPressureMmHg = 750.0)[3]
        val leaving = useCase(pike, fromNormal, normalPressureMmHg = 750.0)[3]

        assertTrue(
            "возврат к норме (${returning.score}) должен быть выше ухода (${leaving.score})",
            returning.score > leaving.score
        )
        assertTrue(returning.factors.first { it.name == "Тенденция" }.comment.contains("норме"))
    }

    @Test
    fun `остывание после жары добавляет кислорода`() {
        val cooling = listOf(
            hour(0, temperature = 30.0), hour(1, temperature = 29.0),
            hour(2, temperature = 28.0), hour(3, temperature = 25.0)
        )
        val heating = listOf(
            hour(0, temperature = 25.0), hour(1, temperature = 26.0),
            hour(2, temperature = 27.0), hour(3, temperature = 30.0)
        )

        val coolingOxygen = useCase(pike, cooling)[3].factors.first { it.name == "Кислород" }
        val heatingOxygen = useCase(pike, heating)[3].factors.first { it.name == "Кислород" }

        assertTrue(
            "остывание (${coolingOxygen.value}) должно давать больше кислорода, чем прогрев (${heatingOxygen.value})",
            coolingOxygen.value > heatingOxygen.value
        )
        assertTrue(coolingOxygen.comment.contains("остывает"))
    }

    @Test
    fun `в тёплой воде амуру лучше чем карпу`() {
        // Случай из разбора: 28 °C — амур кормится, карпу нечем дышать.
        val carp = pike.copy(id = 2, name = "Карп", minTemp = 15f, maxTemp = 28f)
        val grassCarp = pike.copy(id = 3, name = "Белый амур", minTemp = 25f, maxTemp = 30f)
        val warm = (0..5).map { hour(it, temperature = 28.0) }

        val carpScore = useCase(carp, warm).last().score
        val grassCarpScore = useCase(grassCarp, warm).last().score

        assertTrue(
            "амур ($grassCarpScore) должен быть активнее карпа ($carpScore) в тёплой воде",
            grassCarpScore > carpScore
        )
    }

    @Test
    fun `без нормы водоёма ориентиром служит диапазон рыбы`() {
        // Щуке привычны 740–760, середина — 750; давление ровно там.
        val result = useCase(pike, (0..5).map { hour(it) })

        assertTrue(
            result.last().factors.first { it.name == "Давление" }.comment.contains("норма водоёма")
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
