package com.example.fishforecast.domain.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KnowledgeCatalogTest {

    /** Тот самый файл, который едет с приложением. */
    private val asset = File("src/main/assets/knowledge.json").readText()

    private val catalog = KnowledgeCodec.decode(asset).getOrThrow()

    @Test
    fun `встроенные словари читаются`() {
        assertTrue(catalog.waterbodies.isNotEmpty())
        assertTrue(catalog.structures.isNotEmpty())
        assertTrue(catalog.observations.isNotEmpty())
    }

    @Test
    fun `у стоячего пруда кислород проседает к рассвету, у реки нет`() {
        val pond = catalog.waterBody("still_small")!!
        val river = catalog.waterBody("flowing_large")!!

        assertTrue(pond.nightOxygenDropMgL > river.nightOxygenDropMgL)
        assertTrue("река аэрируется течением", river.aeration >= pond.aeration)
        assertTrue("большой водоём инертнее пруда", river.thermalInertia > pond.thermalInertia)
    }

    @Test
    fun `море объявлено, но поведение не описано`() {
        val sea = catalog.waterBody("sea")!!

        assertFalse(sea.behaviorDefined)
    }

    @Test
    fun `структуры значат разное для хищника и мирной рыбы`() {
        val snags = catalog.structure("snags")!!
        val bay = catalog.structure("bay")!!

        assertTrue("коряжник — про хищника", snags.predatorBonus > snags.peacefulBonus)
        assertTrue("заводь — про мирную", bay.peacefulBonus > bay.predatorBonus)
    }

    @Test
    fun `гнилой ил отнимает кислород, а ключ холодит воду`() {
        assertTrue(catalog.structure("rotten_silt")!!.oxygenBonusMgL < 0)
        assertTrue(catalog.structure("spring")!!.waterOffsetC < 0)
        assertTrue(catalog.structure("inflow")!!.oxygenBonusMgL > 0)
    }

    @Test
    fun `наблюдения живут ограниченное время`() {
        catalog.observations.forEach { observation ->
            assertTrue("${observation.id}: срок должен быть задан", observation.hours > 0)
        }
        assertTrue(catalog.observation("rainbow_film")!!.effect < 0)
        assertTrue(catalog.observation("birds_diving")!!.effect > 0)
    }

    @Test
    fun `неизвестный идентификатор не роняет разбор`() {
        // Чужой справочник вправе знать структуры, которых эта сборка не
        // понимает: она просто их не найдёт.
        assertNull(catalog.structure("secret_hole"))
        assertNull(catalog.waterBody(null))
    }

    @Test
    fun `чужой формат отвергается`() {
        assertTrue(KnowledgeCodec.decode("""{"schema":"someapp/1"}""").isFailure)
    }

    @Test
    fun `словари следующего поколения отвергаются`() {
        val future = asset.replace("knowledge/1", "knowledge/2")

        assertTrue(KnowledgeCodec.decode(future).isFailure)
    }

    @Test
    fun `словарь без водоёмов не принимается`() {
        // Пустой ответ сервера не должен оставить расчёт без коэффициентов.
        val empty = """{"schema":"fishforecast.knowledge/1","waterbodies":[]}"""

        assertTrue(KnowledgeCodec.decode(empty).isFailure)
    }

    @Test
    fun `словари переживают запись и чтение`() {
        val restored = KnowledgeCodec.decode(KnowledgeCodec.encode(catalog)).getOrThrow()

        assertEquals(catalog, restored)
    }
}
