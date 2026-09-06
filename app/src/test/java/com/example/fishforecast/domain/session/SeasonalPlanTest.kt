package com.example.fishforecast.domain.session

import com.example.fishforecast.domain.fish.FishCatalogCodec
import com.example.fishforecast.domain.fish.toEntity
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import com.example.fishforecast.domain.light.LightPhase
import com.example.fishforecast.domain.season.SeasonPhase
import com.example.fishforecast.domain.season.SeasonState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * План на выезд под вид и сезон.
 *
 * Раздел собирали заново ровно потому, что он выдавал одно и то же карпу,
 * налиму и щуке. Здесь проверяется, что это больше не так: горизонт берётся у
 * вида, стол — у аппетита, а спящей рыбе план честно говорит, что её сегодня
 * нет.
 */
class SeasonalPlanTest {

    private val knowledge = KnowledgeCodec
        .decode(File("src/main/assets/knowledge.json").readText())
        .getOrThrow()

    private val catalog = FishCatalogCodec
        .decode(File("src/main/assets/initial_fish.json").readText())
        .getOrThrow()

    private fun fish(id: String) = catalog.fish.first { it.id == id }.toEntity()

    private fun conditions(
        shallow: Double,
        oxygen: Double = 8.0,
        phase: LightPhase = LightPhase.EVENING,
        season: SeasonState? = null
    ) = SessionConditions(
        hour = null,
        waterShallowC = shallow,
        waterDeepC = shallow - 2,
        oxygenMgL = oxygen,
        lightPhase = phase,
        hours = emptyList(),
        waterBodyId = "still_small",
        season = season
    )

    private fun state(phase: SeasonPhase, water: Double, drift: Double = 1.0) = SeasonState(
        phase = phase,
        waterC = water,
        driftC = drift,
        overDays = 3,
        daysSinceSpawn = if (phase == SeasonPhase.POST_SPAWN) 2 else null
    )

    private fun plan(id: String, conditions: SessionConditions, methodId: String? = null) =
        buildStrategy(
            input = SessionPlanInput(fish = fish(id), methodId = methodId),
            conditions = conditions,
            knowledge = knowledge
        )

    // ---------- горизонт берётся у вида ----------

    @Test
    fun `горизонт разный у видов в одной и той же воде`() {
        // Раньше все получали дно: справочный горизонт вида не читался вовсе.
        val water = conditions(shallow = 17.0)

        assertEquals("Дно", plan("bream", water).horizon.value)
        assertEquals("Полводы", plan("roach", water).horizon.value)
        assertEquals("Верх", plan("grass_carp", water).horizon.value)
    }

    @Test
    fun `амуру не советуют дно`() {
        // Его ловят с поверхности на камыш и плавающую кукурузу.
        val plan = plan("grass_carp", conditions(shallow = 24.0))

        assertNotEquals("Дно", plan.horizon.value)
    }

    @Test
    fun `в перегретой воде вид поднимается выше своего обычного горизонта`() {
        // Лещ стоит у дна, но при воде выше оптимума ему там душно.
        val hot = conditions(shallow = 27.0, oxygen = 5.0)

        assertEquals("Дно", plan("bream", conditions(shallow = 18.0)).horizon.value)
        assertEquals("Полводы", plan("bream", hot).horizon.value)
    }

    // ---------- сезон ----------

    @Test
    fun `спящему виду плана по насадке нет`() {
        val plan = plan(
            "burbot",
            conditions(shallow = 18.0, season = state(SeasonPhase.DORMANT, 18.0))
        )

        assertNull("налиму летом насадку подбирать незачем", plan.bait)
        assertEquals("Не нужна", plan.groundbait?.value)
        assertTrue(
            "должно быть сказано прямо: ${plan.warnings}",
            plan.warnings.any { it.contains("оцепенении") }
        )
    }

    @Test
    fun `на нересте предупреждают о запрете`() {
        val plan = plan(
            "carp",
            conditions(shallow = 20.0, season = state(SeasonPhase.SPAWN, 20.0))
        )

        assertTrue(
            "запрет региональный, но сказать о нём надо: ${plan.warnings}",
            plan.warnings.any { it.contains("запрет") }
        )
    }

    @Test
    fun `фаза сезона стоит в плане и названа цифрами`() {
        val plan = plan(
            "carp",
            conditions(shallow = 16.0, season = state(SeasonPhase.PRE_SPAWN, 16.0))
        )

        assertEquals("Преднерестовый жор", plan.season?.value)
        assertTrue(
            "причина должна назвать порог вида: ${plan.season?.reason}",
            plan.season!!.reason.contains("18")
        )
    }

    @Test
    fun `без истории воды сезон честно назван неизвестным`() {
        val plan = plan("carp", conditions(shallow = 20.0, season = null))

        assertEquals("неизвестен", plan.season?.value)
    }

    // ---------- аппетит ----------

    @Test
    fun `в оптимуме и на жоре аппетит выше, чем на нересте`() {
        val feast = plan(
            "carp",
            conditions(shallow = 16.0, season = state(SeasonPhase.PRE_SPAWN, 16.0))
        ).appetite
        val spawning = plan(
            "carp",
            conditions(shallow = 20.0, season = state(SeasonPhase.SPAWN, 20.0))
        ).appetite

        assertTrue("жор $feast должен быть выше нереста $spawning", feast > spawning)
    }

    @Test
    fun `у спящего вида аппетита нет вовсе`() {
        val plan = plan(
            "burbot",
            conditions(shallow = 18.0, season = state(SeasonPhase.DORMANT, 18.0))
        )

        assertEquals(0.0, plan.appetite, 0.001)
    }

    @Test
    fun `при низком аппетите стол урезается`() {
        val plan = plan(
            "carp",
            conditions(shallow = 20.0, season = state(SeasonPhase.SPAWN, 20.0)),
            methodId = "feeder_flat"
        )

        assertTrue(
            "объём должен уйти вниз: ${plan.groundbait?.value}",
            plan.groundbait!!.value.contains("не кормить") ||
                plan.groundbait!!.value.contains("мало")
        )
    }

    // ---------- аромат ----------

    @Test
    fun `прикормка говорит, чем пахнуть`() {
        // flavor_profile лежал в справочнике по каждому виду и никуда не шёл.
        val bream = plan(
            "bream",
            conditions(shallow = 20.0, season = state(SeasonPhase.SUMMER, 20.0)),
            methodId = "feeder_flat"
        )

        assertTrue(
            "у леща тёплый стол — карамель и ваниль: ${bream.groundbait?.value}",
            bream.groundbait!!.value.contains("карамель")
        )
    }

    @Test
    fun `аромат не выводится латиницей`() {
        // Ключи справочника не должны просачиваться на экран.
        listOf("carp", "crucian", "bream", "roach").forEach { id ->
            val value = plan(
                id,
                conditions(shallow = 20.0, season = state(SeasonPhase.SUMMER, 20.0)),
                methodId = "feeder_flat"
            ).groundbait?.value.orEmpty()

            assertTrue(
                "«$value» у вида $id содержит непереведённый ключ",
                value.none { it in 'a'..'z' || it in 'A'..'Z' }
            )
        }
    }
}
