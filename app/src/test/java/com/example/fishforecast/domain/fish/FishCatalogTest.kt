package com.example.fishforecast.domain.fish

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.repository.matchesName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FishCatalogTest {

    /** Тот самый файл, который едет с приложением. */
    private val assetCatalog = File("src/main/assets/initial_fish.json").readText()

    @Test
    fun `встроенный справочник читается`() {
        val catalog = FishCatalogCodec.decode(assetCatalog).getOrThrow()

        assertTrue("видов должно быть больше одного", catalog.fish.size > 1)
        val carp = catalog.fish.first { it.id == "carp" }
        assertEquals(18.0, carp.temp.optMin, 0.001)
        assertEquals(30.0, carp.temp.absMax, 0.001)
        assertEquals(5.0, carp.oxygen.comfortMgL, 0.001)
        assertTrue(carp.baits.warm.isNotEmpty())
        assertTrue(carp.groundbaitRules.warm.notes.isNotBlank())
    }

    @Test
    fun `справочник с сервера приходит с версией`() {
        val wrapped = """
            {
              "schema": "fishforecast.fish-catalog/1",
              "version": 7,
              "fish": $assetCatalog
            }
        """.trimIndent()

        val catalog = FishCatalogCodec.decode(wrapped).getOrThrow()

        assertEquals(7, catalog.version)
        assertEquals(10, catalog.fish.size)
    }

    @Test
    fun `справочник следующего поколения отвергается`() {
        val future = """{"schema":"fishforecast.fish-catalog/2","fish":$assetCatalog}"""

        assertTrue(FishCatalogCodec.decode(future).isFailure)
    }

    @Test
    fun `пустой справочник не принимается`() {
        // Пустой ответ сервера не должен стирать то, что уже есть.
        assertTrue(FishCatalogCodec.decode("[]").isFailure)
    }

    @Test
    fun `вид переносится в базу без потерь`() {
        val carp = FishCatalogCodec.decode(assetCatalog).getOrThrow().fish.first { it.id == "carp" }

        val entity = carp.toEntity()

        assertEquals("carp", entity.uid)
        assertEquals(18f, entity.optMinTemp)
        assertEquals(30f, entity.absMaxTemp)
        assertEquals(3f, entity.oxygenCriticalMgL)
        assertEquals(carp.baits.cold, entity.baitsCold.decodeBaits())
        assertEquals(carp.groundbaitRules.warm.notes, entity.groundbaitWarm.decodeGroundbait().notes)
    }

    @Test
    fun `обновление не затирает своё описание`() {
        val catalog = FishCatalogCodec.decode(assetCatalog).getOrThrow()
        val carp = catalog.fish.first { it.id == "carp" }
        val mine = carp.toEntity().copy(id = 5, description = "Мой пруд, берёт у камыша")

        val updated = carp.toEntity(mine)

        assertEquals(5, updated.id)
        assertEquals("Мой пруд, берёт у камыша", updated.description)
    }

    @Test
    fun `вид туда и обратно остаётся собой`() {
        val carp = FishCatalogCodec.decode(assetCatalog).getOrThrow().fish.first { it.id == "carp" }

        val restored = carp.toEntity().toCatalogFish()

        assertEquals(carp.id, restored.id)
        assertEquals(carp.temp, restored.temp)
        assertEquals(carp.oxygen, restored.oxygen)
        assertEquals(carp.baits, restored.baits)
        assertEquals(carp.groundbaitRules, restored.groundbaitRules)
    }

    @Test
    fun `у каждого вида есть гильдия и профиль света`() {
        val catalog = FishCatalogCodec.decode(assetCatalog).getOrThrow()

        catalog.fish.forEach { fish ->
            assertTrue(
                "${fish.id}: гильдия должна быть указана",
                fish.guild == "predator" || fish.guild == "peaceful"
            )
            assertEquals("${fish.id}: шесть фаз света", 6, fish.lightActivity.size)
        }

        assertEquals("predator", catalog.fish.first { it.id == "pike" }.guild)
        assertEquals("peaceful", catalog.fish.first { it.id == "carp" }.guild)
    }

    @Test
    fun `ночные виды отличаются от дневных профилем света`() {
        val catalog = FishCatalogCodec.decode(assetCatalog).getOrThrow()
        val burbot = catalog.fish.first { it.id == "burbot" }
        val roach = catalog.fish.first { it.id == "roach" }

        assertTrue("налим ночной", burbot.lightActivity["night"]!! > burbot.lightActivity["day"]!!)
        assertTrue("плотва дневная", roach.lightActivity["day"]!! > roach.lightActivity["night"]!!)
    }

    @Test
    fun `гильдия и структуры переживают перенос в базу и обратно`() {
        val pike = FishCatalogCodec.decode(assetCatalog).getOrThrow().fish.first { it.id == "pike" }

        val restored = pike.toEntity().toCatalogFish()

        assertEquals(pike.guild, restored.guild)
        assertEquals(pike.lightActivity, restored.lightActivity)
        assertEquals(pike.preferredStructures, restored.preferredStructures)
    }

    @Test
    fun `виды ссылаются только на известные структуры`() {
        // Справочник видов и словарь структур правятся порознь: расхождение
        // здесь означает опечатку, которую иначе заметит только рыболов.
        val catalog = FishCatalogCodec.decode(assetCatalog).getOrThrow()
        val known = com.example.fishforecast.domain.knowledge.KnowledgeCodec
            .decode(File("src/main/assets/knowledge.json").readText())
            .getOrThrow()
            .structures
            .map { it.id }
            .toSet()

        catalog.fish.forEach { fish ->
            fish.preferredStructures.forEach { id ->
                assertTrue("${fish.id}: неизвестная структура $id", id in known)
            }
        }
    }

    @Test
    fun `старая запись узнаётся по имени`() {
        // До общего справочника у видов были случайные идентификаторы. После
        // обновления рыболов не должен получить двух щук.
        assertTrue("Карп".matchesName("Карп / Сазан"))
        assertTrue("Карп / Сазан".matchesName("карп"))
        assertFalse("Карась".matchesName("Карп / Сазан"))
    }

    @Test
    fun `у вида без справочника остаются рабочие значения`() {
        // Рыболов завёл вид руками: наживок нет, но приложение не должно
        // падать на пустых полях.
        val custom = FishEntity(
            name = "Голавль",
            optMinTemp = 14f,
            optMaxTemp = 22f,
            absMinTemp = 6f,
            absMaxTemp = 28f,
            minPressure = 740f,
            maxPressure = 760f
        )

        assertEquals(emptyList<String>(), custom.baitsCold.decodeBaits())
        assertEquals("none", custom.groundbaitWarm.decodeGroundbait().volume)
    }
}
