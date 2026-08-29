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
    fun `схема закорма под трофея отличается от схемы под количество`() {
        val carpet = catalog.baitingPlan("carpet")!!
        val program = catalog.baitingPlan("program")!!

        assertEquals("numbers", carpet.goal)
        assertEquals("trophy", program.goal)
        assertTrue("трофейную насадку сушат", program.hardened)
        assertFalse("под стаю сушить нечего", carpet.hardened)
        assertTrue("программу начинают заранее", program.primeDays > 0)
        assertTrue("у каждой схемы задан размер насадки", catalog.baitingPlans.all {
            it.baitSizeMm.isNotBlank()
        })
    }

    @Test
    fun `в холодной воде есть схема на обе цели`() {
        // Когда рыба ест мало, спорить о цели бессмысленно: работает точка.
        val cold = catalog.baitingPlans.filter { it.water == "cold" }

        assertTrue("холодная вода описана", cold.isNotEmpty())
        assertTrue("схема не привязана к цели", cold.any { it.goal == "any" })
    }

    @Test
    fun `коряжник и ил говорят, чего требуют от снасти`() {
        // Место опасно не для рыбы, а для монтажа, и узнать об этом надо
        // дома, а не на берегу.
        assertTrue(catalog.structure("snags")!!.gearNote.isNotBlank())
        assertTrue(catalog.structure("gravel_silt_edge")!!.gearNote.isNotBlank())
        assertTrue("обычной воде требовать нечего", catalog.structure("water")!!.gearNote.isBlank())
    }

    @Test
    fun `карповая донка засекает рыбу сама, поплавок — нет`() {
        val carp = catalog.method("carp_bottom")!!
        val float = catalog.method("float")!!

        assertTrue("грузило должно быть тяжёлым", carp.minLeadG >= 100)
        assertTrue("монтаж описан", carp.rig.isNotBlank())
        assertEquals("поплавок засекает рукой", 0, float.minLeadG)
    }

    @Test
    fun `неизвестный идентификатор не роняет разбор`() {
        // Чужой справочник вправе знать структуры, которых эта сборка не
        // понимает: она просто их не найдёт.
        assertNull(catalog.structure("secret_hole"))
        assertNull(catalog.waterBody(null))
        assertNull(catalog.baitingPlan("secret_recipe"))
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
