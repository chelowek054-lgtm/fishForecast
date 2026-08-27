package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.water.WaterHour
import com.example.fishforecast.domain.water.WaterState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateFishActivityUseCaseTest {

    private val useCase = CalculateFishActivityUseCase()

    /** Щука: оптимум 8–18 °C, предел 2–22 °C, 740–760 мм рт. ст. */
    private val pike = FishEntity(
        id = 1,
        name = "Щука",
        description = "",
        guild = "predator",
        optMinTemp = 8f,
        optMaxTemp = 18f,
        absMinTemp = 2f,
        absMaxTemp = 22f,
        minPressure = 740f,
        maxPressure = 760f
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

    /** Ход воды, посчитанный где-то снаружи; здесь он задаётся руками. */
    private fun water(vararg temperatures: Double): WaterState {
        val hours = temperatures.mapIndexed { index, value ->
            WaterHour(time = "2026-08-26T%02d:00".format(index), temperature = value)
        }
        return WaterState(
            shallow = hours,
            deep = hours,
            shallowDepthM = 1.5,
            deepDepthM = 4.0,
            depthsAssumed = false,
            anchored = false
        )
    }

    @Test
    fun `решает температура воды, а не воздуха`() {
        // Воздух прогрелся, вода ещё холодная — рыба живёт в воде.
        val forecast = (0..5).map { hour(it, temperature = 30.0) }

        val byAir = useCase(pike, forecast).last().score
        val byWater = useCase(
            fish = pike,
            forecast = forecast,
            water = water(14.0, 14.0, 14.0, 14.0, 14.0, 14.0)
        ).last().score

        assertTrue("вода в комфорте должна поднять оценку: $byWater vs $byAir", byWater > byAir)
    }

    @Test
    fun `духота в прогретой воде гасит клёв`() {
        val forecast = (0..5).map { hour(it) }

        val cool = useCase(pike, forecast, water = water(16.0, 16.0, 16.0, 16.0, 16.0, 16.0))
        val hot = useCase(pike, forecast, water = water(31.0, 31.0, 31.0, 31.0, 31.0, 31.0))

        assertTrue(
            "в тёплой воде кислорода не хватает: ${hot.last().score} vs ${cool.last().score}",
            hot.last().score < cool.last().score
        )
    }

    @Test
    fun `остывающая вода ценится выше стоячей`() {
        // Оба ряда заканчиваются одной температурой — чуть выше оптимума,
        // где кислорода уже впритык. Разница только в том, что в одном
        // случае вода пришла к ней сверху: значит, кислород прибывает.
        val carp = pike.copy(
            name = "Карп",
            optMinTemp = 18f,
            optMaxTemp = 26f,
            absMinTemp = 8f,
            absMaxTemp = 30f
        )
        val forecast = (0..5).map { hour(it, temperature = 27.0) }

        val steady = useCase(carp, forecast, water = water(27.5, 27.5, 27.5, 27.5, 27.5, 27.5))
        val cooling = useCase(carp, forecast, water = water(29.0, 28.7, 28.4, 28.1, 27.8, 27.5))

        assertTrue(
            "остывание добавляет кислорода: ${cooling.last().score} vs ${steady.last().score}",
            cooling.last().score > steady.last().score
        )
    }

    /** Рябь: ветер, который не мешает ни хищнику, ни мирной рыбе. */
    private val RIPPLE_KMH = 12.0

    /** Летние сутки под Москвой: рассвет в 5:11, закат в 20:30. */
    private fun sunTimes(date: String = "2026-08-26") = listOf(
        DailySunEntity(
            mapId = 1,
            date = date,
            sunrise = "${date}T05:11",
            sunset = "${date}T20:30"
        )
    )

    @Test
    fun `хищник и мирная рыба в одних условиях получают разные оценки`() {
        // Ровно та беда, ради которой всё затевалось: до появления света
        // щука и карп на одной воде получали одинаковый балл.
        val carp = pike.copy(
            id = 2,
            name = "Карп",
            guild = "peaceful",
            optMinTemp = 8f,
            optMaxTemp = 18f
        )
        val predator = pike
        // Ветер держим в полосе ряби, чтобы сравнивать именно свет: штиль
        // хищнику мешает сильнее, и он съел бы разницу.
        val dawn = (0..23).map { hour(it, windSpeed = RIPPLE_KMH) }

        val atDawn = { fish: FishEntity ->
            useCase(fish, dawn, sunTimes = sunTimes())
                .first { it.time == "2026-08-26T05:00" }
                .score
        }

        assertTrue(
            "на рассвете хищник должен опережать мирную рыбу: " +
                "${atDawn(predator)} против ${atDawn(carp)}",
            atDawn(predator) > atDawn(carp)
        )
    }

    @Test
    fun `у щуки разброс по времени суток больше, чем у карпа`() {
        val carp = pike.copy(id = 2, name = "Карп", guild = "peaceful")
        val forecast = (0..23).map { hour(it, windSpeed = RIPPLE_KMH) }

        fun spread(fish: FishEntity): Int {
            val day = useCase(fish, forecast, sunTimes = sunTimes())
            val dawn = day.first { it.time == "2026-08-26T05:00" }.score
            val noon = day.first { it.time == "2026-08-26T13:00" }.score
            return dawn - noon
        }

        assertTrue(
            "разброс щуки ${spread(pike)} должен быть больше разброса карпа ${spread(carp)}",
            spread(pike) > spread(carp)
        )
    }

    @Test
    fun `ночной вид берёт ночью, а не на заре`() {
        val burbot = pike.copy(
            name = "Налим",
            guild = "predator",
            lightActivity = """{"night":1.0,"dawn":0.6,"morning":0.3,"day":0.2,"evening":0.5,"dusk":0.8}"""
        )
        val forecast = (0..23).map { hour(it, windSpeed = RIPPLE_KMH) }

        val result = useCase(burbot, forecast, sunTimes = sunTimes())
        val night = result.first { it.time == "2026-08-26T02:00" }.score
        val dawn = result.first { it.time == "2026-08-26T05:00" }.score

        assertTrue("свой профиль вида старше правила про зори: $night против $dawn", night > dawn)
    }

    @Test
    fun `без данных о солнце оценка не проседает`() {
        // Фактор света просто не участвует, а не обнуляет треть веса.
        val forecast = (0..5).map { hour(it) }

        val withoutSun = useCase(pike, forecast).last().score

        assertTrue("оценка должна остаться разумной: $withoutSun", withoutSun >= 80)
    }

    @Test
    fun `рябь лучше зеркальной глади`() {
        val calm = useCase(pike, (0..5).map { hour(it, windSpeed = 1.0) }).last().score
        val ripple = useCase(pike, (0..5).map { hour(it, windSpeed = 12.0) }).last().score

        assertTrue("рябь прячет рыболова и ломает свет: $ripple против $calm", ripple > calm)
    }

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
    fun `давление вдали от нормы места снижает оценку`() {
        val atNormal = useCase(
            pike,
            (0..5).map { hour(it) },
            normalPressureMmHg = 750.0
        ).last().score
        val lowPressure = useCase(
            pike,
            (0..5).map { hour(it, pressureHpa = 720.0 * 1.333224) },
            normalPressureMmHg = 750.0
        ).last().score

        assertTrue(
            "давление ниже нормы должно снижать оценку: $lowPressure vs $atNormal",
            lowPressure < atNormal
        )
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
        val carp = pike.copy(
            id = 2,
            name = "Карп",
            optMinTemp = 18f,
            optMaxTemp = 26f,
            absMinTemp = 8f,
            absMaxTemp = 30f,
            oxygenComfortMgL = 5f,
            oxygenCriticalMgL = 3f
        )
        val grassCarp = pike.copy(
            id = 3,
            name = "Белый амур",
            optMinTemp = 25f,
            optMaxTemp = 30f,
            absMinTemp = 12f,
            absMaxTemp = 34f,
            oxygenComfortMgL = 4f,
            oxygenCriticalMgL = 2.5f
        )
        val warm = (0..5).map { hour(it, temperature = 28.0) }

        val carpScore = useCase(carp, warm).last().score
        val grassCarpScore = useCase(grassCarp, warm).last().score

        assertTrue(
            "амур ($grassCarpScore) должен быть активнее карпа ($carpScore) в тёплой воде",
            grassCarpScore > carpScore
        )
    }

    @Test
    fun `без нормы места давление не оценивается`() {
        val result = useCase(pike, (0..5).map { hour(it) })

        val pressure = result.last().factors.first { it.name == "Давление" }
        assertTrue(
            "должно быть сказано, что нормы ещё нет: ${pressure.comment}",
            pressure.comment.contains("не посчитана")
        )
    }

    @Test
    fun `норма не зависит от диапазона давления рыбы`() {
        // Раньше без нормы места подставлялась середина диапазона рыбы, и
        // две рыбы с одинаковым комфортом по температуре получали разную
        // оценку из-за справочника. Норма — свойство места, рыба ни при чём.
        val lowlandFish = pike.copy(minPressure = 700f, maxPressure = 720f)
        val forecast = (0..5).map { hour(it) }

        assertEquals(
            useCase(pike, forecast).last().score,
            useCase(lowlandFish, forecast).last().score
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
