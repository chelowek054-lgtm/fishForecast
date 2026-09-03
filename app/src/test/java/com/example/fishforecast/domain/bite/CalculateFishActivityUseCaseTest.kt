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

    // --- Динамика погоды: откуда пришли, а не только что сейчас ---

    /** Сутки часов с заданным ходом давления, чтобы окно в 24 часа заполнилось. */
    private fun day(
        pressureFrom: Double,
        pressureTo: Double,
        hours: Int = 25,
        windSpeed: Double = 12.0,
        windDirection: Double = 180.0
    ): List<WeatherEntity> = (0 until hours).map { index ->
        val part = index.toDouble() / (hours - 1)
        hour(
            index = index % 24,
            pressureHpa = (pressureFrom + (pressureTo - pressureFrom) * part) * 1.333224,
            windSpeed = windSpeed
        ).copy(
            time = "2026-08-%02dT%02d:00".format(26 + index / 24, index % 24),
            windDirection = windDirection
        )
    }

    @Test
    fun `сутки падения давления лучше суток роста`() {
        // Перед фронтом рыба кормится впрок, после него — прижата ко дну.
        // В последний час давление одинаковое: разницу даёт только история.
        val falling = useCase(pike, day(pressureFrom = 754.0, pressureTo = 750.0), 750.0)
        val rising = useCase(pike, day(pressureFrom = 745.0, pressureTo = 750.0), 750.0)

        assertTrue(
            "падение должно быть выше роста: ${falling.last().score} против ${rising.last().score}",
            falling.last().score > rising.last().score
        )
    }

    @Test
    fun `суточный ход назван причиной, а не спрятан в балле`() {
        val plan = useCase(pike, day(pressureFrom = 756.0, pressureTo = 750.0), 750.0).last()

        val factor = plan.factors.first { it.name == "Ход за сутки" }
        assertTrue("должно быть сказано про фронт: ${factor.comment}", factor.comment.contains("фронт"))
    }

    @Test
    fun `без истории суточный ход не штрафует`() {
        // Первые часы прогноза не должны выглядеть хуже только потому,
        // что смотреть назад ещё некуда.
        val short = useCase(pike, (0..2).map { hour(it) }, 750.0).last()

        val factor = short.factors.first { it.name == "Ход за сутки" }
        assertEquals(1.0, factor.value, 0.001)
        assertTrue(factor.comment.contains("Истории меньше"))
    }

    @Test
    fun `холодная вода, которая греется, лучше стынущей`() {
        // Было холодно, стало теплее — рыба выходит кормиться. Прежняя
        // модель штрафовала любой прогрев, потому что считала его только
        // через кислород.
        val forecast = (0..8).map { hour(it) }
        val warming = useCase(pike, forecast, 750.0, water(3.0, 3.2, 3.4, 3.6, 3.9, 4.2, 4.6, 5.0, 5.4))
        val cooling = useCase(pike, forecast, 750.0, water(7.0, 6.6, 6.2, 5.9, 5.7, 5.6, 5.5, 5.4, 5.4))

        assertTrue(
            "прогрев холодной воды должен быть выше: ${warming.last().score} против ${cooling.last().score}",
            warming.last().score > cooling.last().score
        )
    }

    @Test
    fun `в перегретой воде знак хода меняется на обратный`() {
        val forecast = (0..8).map { hour(it) }
        val cooling = useCase(pike, forecast, 750.0, water(24.0, 23.6, 23.2, 22.9, 22.6, 22.3, 22.0, 21.7, 21.4))
        val warming = useCase(pike, forecast, 750.0, water(19.0, 19.4, 19.8, 20.2, 20.6, 21.0, 21.4, 21.8, 22.2))

        assertTrue(
            "остывание жары должно быть выше прогрева: ${cooling.last().score} против ${warming.last().score}",
            cooling.last().score > warming.last().score
        )
    }

    @Test
    fun `сменившийся ветер хуже устойчивого`() {
        // Разворот означает прошедший фронт: корм понесло в другую сторону,
        // и рыбе надо заново искать стол.
        val steady = day(750.0, 750.0, windDirection = 180.0)
        val turned = steady.mapIndexed { index, entity ->
            if (index >= steady.size - 3) entity.copy(windDirection = 20.0) else entity
        }

        val steadyScore = useCase(pike, steady, 750.0).last()
        val turnedScore = useCase(pike, turned, 750.0).last()

        assertTrue(
            "разворот должен стоить баллов: ${turnedScore.score} против ${steadyScore.score}",
            turnedScore.score < steadyScore.score
        )
        assertTrue(
            turnedScore.factors.first { it.name == "Ветер" }.comment.contains("развернулся")
        )
    }

    /** Ровная вода на те же часы, что и сутки прогноза. */
    private fun waterFor(forecast: List<WeatherEntity>, temperature: Double): WaterState {
        val hours = forecast.map { WaterHour(time = it.time, temperature = temperature) }
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
    fun `северный ветер в холодной воде хуже южного`() {
        val northForecast = day(750.0, 750.0, windDirection = 0.0)
        val southForecast = day(750.0, 750.0, windDirection = 180.0)
        val cold = waterFor(northForecast, 5.0)
        val north = useCase(pike, northForecast, 750.0, cold).last()
        val south = useCase(pike, southForecast, 750.0, cold).last()

        assertTrue(
            "северный должен быть хуже: ${north.score} против ${south.score}",
            north.score < south.score
        )
        assertTrue(north.factors.first { it.name == "Ветер" }.comment.contains("студит"))
    }

    @Test
    fun `северный ветер на перегретой воде идёт в плюс`() {
        val forecast = day(750.0, 750.0, windDirection = 0.0)
        val north = useCase(pike, forecast, 750.0, waterFor(forecast, 21.0)).last()

        assertTrue(
            "северный по жаре должен помогать: ${north.factors.first { it.name == "Ветер" }.comment}",
            north.factors.first { it.name == "Ветер" }.comment.contains("сбивает жару")
        )
    }

    @Test
    fun `штиль на перегретой воде расслаивает её, и мель проигрывает яме`() {
        val calm = day(750.0, 750.0, windSpeed = 2.0)
        val hot = waterFor(calm, 21.0)

        val shallow = useCase(pike, calm, 750.0, hot, place = PlaceContext(WaterLayerChoice.SHALLOW)).last()
        val deep = useCase(pike, calm, 750.0, hot, place = PlaceContext(WaterLayerChoice.DEEP)).last()

        val factor = shallow.factors.first { it.name == "Расслоение" }
        assertTrue("должно быть сказано про термоклин: ${factor.comment}", factor.comment.contains("расслоилась"))
        assertTrue("в яме должно быть не хуже: ${deep.score} против ${shallow.score}", deep.score >= shallow.score)
    }

    @Test
    fun `при ветре расслоения нет`() {
        // Ветер перемешивает столб: слоёв не образуется, и штрафа быть не должно.
        val forecast = day(750.0, 750.0, windSpeed = 18.0)
        val windy = useCase(pike, forecast, 750.0, waterFor(forecast, 21.0)).last()

        assertTrue(windy.factors.none { it.name == "Расслоение" })
    }
}
