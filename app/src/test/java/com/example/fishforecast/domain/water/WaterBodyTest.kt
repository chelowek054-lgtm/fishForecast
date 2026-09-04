package com.example.fishforecast.domain.water

import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDateTime

class WaterBodyTest {

    private val knowledge = KnowledgeCodec
        .decode(File("src/main/assets/knowledge.json").readText())
        .getOrThrow()

    private val pond = knowledge.waterBody("still_small")!!
    private val river = knowledge.waterBody("flowing_large")!!

    /** Сутки погоды с суточным ходом температуры и солнца. */
    private fun day(start: LocalDateTime, meanAir: Double, windKmh: Double = 10.0) =
        (0 until 24).map { hour ->
            val daylight = if (hour in 6..18) {
                kotlin.math.sin((hour - 6) / 12.0 * Math.PI)
            } else {
                0.0
            }
            WeatherEntity(
                mapId = 1,
                time = start.plusHours(hour.toLong()).toString().take(16),
                temperature = meanAir + 5 * daylight - 2.5,
                humidity = 70.0,
                pressure = 1005.0,
                windSpeed = windKmh,
                shortwaveRadiation = 600.0 * daylight,
                weatherCode = 0,
                latitude = 55.0,
                longitude = 37.0
            )
        }

    private fun days(count: Int, meanAir: Double, fromDay: Int = 0) =
        (0 until count).flatMap {
            day(LocalDateTime.of(2026, 8, 20, 0, 0).plusDays((fromDay + it).toLong()), meanAir)
        }

    @Test
    fun `река инертнее пруда`() {
        val hours = days(6, meanAir = 26.0) + days(2, meanAir = 10.0, fromDay = 6)

        val inPond = simulateWaterTemperature(hours, WaterLayer(2.0), waterBody = pond)
        val inRiver = simulateWaterTemperature(hours, WaterLayer(2.0), waterBody = river)

        val pondDrop = inPond[hours.size - 49].temperature - inPond.last().temperature
        val riverDrop = inRiver[hours.size - 49].temperature - inRiver.last().temperature

        assertTrue("пруд остывает быстрее реки: $pondDrop против $riverDrop", pondDrop > riverDrop)
    }

    @Test
    fun `в проточной воде кислорода больше, чем в стоячей`() {
        val warm = 24.0

        val inPond = availableOxygenMgL(warm, pond)
        val inRiver = availableOxygenMgL(warm, river)

        assertTrue("река аэрируется сама: $inRiver против $inPond", inRiver > inPond)
        assertTrue("потолок остаётся насыщением", inRiver <= oxygenSaturationMgL(warm) + 0.001)
    }

    @Test
    fun `к рассвету в пруду кислорода меньше, чем днём`() {
        val evening = availableOxygenMgL(22.0, pond, darkHours = 0)
        val dawn = availableOxygenMgL(22.0, pond, darkHours = 8)

        assertTrue("ночью растения дышат: $dawn против $evening", dawn < evening)
        assertEquals(pond.nightOxygenDropMgL, evening - dawn, 0.05)
    }

    @Test
    fun `на реке ночного провала нет`() {
        val evening = availableOxygenMgL(22.0, river, darkHours = 0)
        val dawn = availableOxygenMgL(22.0, river, darkHours = 8)

        assertEquals(evening, dawn, 0.001)
    }

    @Test
    fun `ветер подгоняет стоячую воду к насыщению`() {
        val calm = availableOxygenMgL(24.0, pond, windMs = 0.0)
        val windy = availableOxygenMgL(24.0, pond, windMs = 8.0)

        assertTrue("рябь аэрирует: $windy против $calm", windy > calm)
    }

    @Test
    fun `без словаря расчёт остаётся прежним`() {
        // Район без выбранного типа не должен вести себя иначе, чем до
        // появления словарей: кислород равен насыщению.
        assertEquals(oxygenSaturationMgL(20.0), availableOxygenMgL(20.0, null), 0.001)
    }

    @Test
    fun `состояние воды считает кислород по часам`() {
        val hours = days(3, meanAir = 22.0)

        val state = calculateWaterState(hours, null, pond)

        assertEquals(hours.size, state.oxygen.size)
        val night = state.oxygenAt(hours.first { it.time.endsWith("T04:00") }.time)!!
        val afternoon = state.oxygenAt(hours.first { it.time.endsWith("T15:00") }.time)!!
        assertTrue("под утро кислорода меньше: $night против $afternoon", night < afternoon)
    }

    @Test
    fun `в расслоившемся пруду яма теряет кислород, а мель нет`() {
        // Столб разделился: наверху ветер и водоросли, внизу термоклин и
        // донный расход. Брать для ямы кислород мели значило бы завышать её
        // шансы ровно там, где рыбе нечем дышать.
        val warm = 22.0
        val cold = 12.0

        val mixed = deepOxygenMgL(deepTemperatureC = 20.0, shallowTemperatureC = 21.0, waterBody = pond)
        val fresh = deepOxygenMgL(cold, warm, pond, stratifiedHours = 0)
        val stale = deepOxygenMgL(cold, warm, pond, stratifiedHours = 48)

        assertTrue("расслоение должно тратить кислород: $stale против $fresh", stale < fresh)
        assertEquals("полтора миллиграмма за сутки, двое суток — три", 3.0, fresh - stale, 0.05)
        assertTrue("перемешанный столб дышит вместе с мелью", mixed > 0.0)
    }

    @Test
    fun `на реке расслоения не бывает`() {
        val fresh = deepOxygenMgL(12.0, 22.0, river, stratifiedHours = 0)
        val stale = deepOxygenMgL(12.0, 22.0, river, stratifiedHours = 72)

        assertEquals("течение не даёт слоям разойтись", fresh, stale, 0.001)
    }

    @Test
    fun `три градуса между слоями — уже термоклин`() {
        assertTrue(isStratified(shallowTemperatureC = 22.0, deepTemperatureC = 19.0))
        assertTrue(!isStratified(shallowTemperatureC = 22.0, deepTemperatureC = 20.0))
    }

    @Test
    fun `кислород считается для обоих слоёв`() {
        // Жаркая неделя: мель прогревается, яма отстаёт — слои расходятся.
        val hours = days(7, meanAir = 27.0)
        val map = com.example.fishforecast.data.local.entities.SavedMapEntity(
            name = "Пруд",
            offlineRegionId = 0,
            north = 55.8, south = 55.7, east = 37.7, west = 37.6,
            minZoom = 10.0, maxZoom = 14.0,
            shallowDepthM = 1.0,
            deepDepthM = 6.0
        )

        val state = calculateWaterState(hours, map, pond)
        val last = state.shallow.last().time

        val shallowOxygen = state.oxygenAt(last)!!
        val deepOxygen = state.oxygenAt(last, com.example.fishforecast.domain.bite.WaterLayerChoice.DEEP)!!

        assertTrue("у ямы должен быть свой кислород", state.oxygenDeep.isNotEmpty())
        assertTrue(
            "в прогретом пруду яме должно быть не легче: $deepOxygen против $shallowOxygen",
            deepOxygen != shallowOxygen
        )
    }
}
