package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlaceContextTest {

    /** Тот самый словарь, который едет с приложением. */
    private val catalog = KnowledgeCodec
        .decode(File("src/main/assets/knowledge.json").readText())
        .getOrThrow()

    private fun spot(structures: String) = FishingSpotEntity(
        id = 1,
        name = "Мысок",
        latitude = 55.0,
        longitude = 37.0,
        structures = structures
    )

    @Test
    fun `структуры точки доходят до места`() {
        // Ради этого точку и сохраняют: коряжник и бровка — то, чем место
        // отличается от соседнего.
        val place = placeOf(
            spot = spot("""["snags","drop_off"]"""),
            layer = WaterLayerChoice.SHALLOW,
            catalog = catalog
        )

        assertEquals(listOf("snags", "drop_off"), place.structures.map { it.id })
        assertEquals("Мысок", place.title)
    }

    @Test
    fun `коряжник помогает хищнику сильнее, чем мирной рыбе`() {
        val place = placeOf(spot("""["snags"]"""), WaterLayerChoice.SHALLOW, catalog)

        val predator = place.bonusFor(Guild.PREDATOR)
        val peaceful = place.bonusFor(Guild.PEACEFUL)

        assertEquals(1.25, predator, 0.001)
        assertEquals(1.10, peaceful, 0.001)
        assertTrue(predator > peaceful)
    }

    @Test
    fun `гнилой ил работает против обоих`() {
        val place = placeOf(spot("""["rotten_silt"]"""), WaterLayerChoice.DEEP, catalog)

        assertTrue(place.bonusFor(Guild.PREDATOR) < 1.0)
        assertTrue(place.bonusFor(Guild.PEACEFUL) < 1.0)
        assertEquals(-1.5, place.oxygenOffsetMgL, 0.001)
    }

    @Test
    fun `незнакомая структура не роняет место`() {
        // Словарь правится отдельно от точек: чужой идентификатор не повод
        // остаться без оценки.
        val place = placeOf(spot("""["snags","secret_hole"]"""), WaterLayerChoice.SHALLOW, catalog)

        assertEquals(listOf("snags"), place.structures.map { it.id })
    }

    @Test
    fun `без точки остаётся один слой`() {
        val place = placeOf(spot = null, layer = WaterLayerChoice.DEEP, catalog = catalog)

        assertTrue(place.structures.isEmpty())
        assertEquals(1.0, place.bonusFor(Guild.PREDATOR), 0.001)
        assertEquals("в яме", place.title)
    }

    @Test
    fun `место не заменяет собой погоду и не обнуляет шанс`() {
        // Даже самое рыбное место остаётся множителем, а не приговором.
        val best = placeOf(
            spot("""["snags","hump","drop_off","point","bridge_piles"]"""),
            WaterLayerChoice.SHALLOW,
            catalog
        )
        val worst = placeOf(spot("""["rotten_silt","rotten_silt"]"""), WaterLayerChoice.SHALLOW, catalog)

        assertEquals(1.6, best.bonusFor(Guild.PREDATOR), 0.001)
        assertEquals(0.4, worst.bonusFor(Guild.PREDATOR), 0.001)
    }

    @Test
    fun `донный ключ холодит место, приток добавляет кислорода`() {
        val spring = placeOf(spot("""["spring","inflow"]"""), WaterLayerChoice.SHALLOW, catalog)

        assertEquals(-3.0, spring.waterOffsetC, 0.001)
        assertEquals(0.5, spring.oxygenOffsetMgL, 0.001)
    }
}
