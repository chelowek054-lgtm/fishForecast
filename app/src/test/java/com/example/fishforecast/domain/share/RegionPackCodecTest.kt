package com.example.fishforecast.domain.share

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.SpotPlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionPackCodecTest {

    private val map = SavedMapEntity(
        id = 7,
        uid = "region-1",
        name = "Озеро",
        offlineRegionId = 42,
        north = 55.8,
        south = 55.7,
        east = 37.7,
        west = 37.6,
        minZoom = 10.0,
        maxZoom = 14.0,
        sizeBytes = 12345,
        waterBodyType = "flowing_large",
        baselinePressureMmHg = 745.8,
        elevationM = 152.0,
        shallowDepthM = 1.5,
        deepDepthM = 5.0
    )

    private val carp = FishEntity(
        id = 3,
        uid = "fish-carp",
        name = "Карп",
        description = "",
        optMinTemp = 18f,
        optMaxTemp = 26f,
        absMinTemp = 8f,
        absMaxTemp = 30f,
        minPressure = 740f,
        maxPressure = 760f
    )

    private val spot = FishingSpotEntity(
        id = 11,
        uid = "spot-1",
        name = "Мысок",
        latitude = 55.78,
        longitude = 37.62,
        fishId = 3,
        note = "Бровка вдоль камыша",
        placement = SpotPlacement.SHORE.name,
        structures = "[\"drop_off\",\"reeds\"]"
    )

    private fun pack() = RegionPackCodec.build(
        map = map,
        spots = listOf(spot),
        fish = listOf(carp),
        packId = "pack-1",
        createdAt = "2026-08-27T12:00",
        author = PackAuthor(id = "author-1", name = "Рыболов")
    )

    @Test
    fun `пакет переживает запись и чтение`() {
        val restored = RegionPackCodec.decode(RegionPackCodec.encode(pack())).getOrThrow()

        assertEquals("region-1", restored.region.id)
        assertEquals(745.8, restored.region.normalPressureMmHg!!, 0.001)
        assertEquals(1.5, restored.region.shallowDepthM!!, 0.001)
        assertEquals("flowing_large", restored.region.waterBodyType)
        assertEquals("SHORE", restored.spots.first().placement)
        assertEquals(listOf("drop_off", "reeds"), restored.spots.first().structures)
    }

    @Test
    fun `точка ссылается на вид глобальным идентификатором`() {
        // Числовой ключ справочника у получателя свой, поэтому в пакете
        // должен ехать uid, а не id.
        assertEquals("fish-carp", pack().spots.first().fishId)
    }

    @Test
    fun `разбор возвращает сущности без числовых ключей`() {
        val contents = RegionPackCodec.toEntities(pack())

        assertEquals("region-1", contents.map.uid)
        assertEquals(0, contents.map.id)
        assertEquals(0, contents.spots.first().id)
        // Своя область MapLibre у получателя появится при скачивании тайлов.
        assertEquals(0L, contents.map.offlineRegionId)
        assertEquals("fish-carp", contents.spotFish["spot-1"])
    }

    @Test
    fun `чужой формат отвергается, а не читается наполовину`() {
        val alien = """{"schema":"someapp/1","id":"x","createdAt":"","region":{}}"""

        assertTrue(RegionPackCodec.decode(alien).isFailure)
    }

    @Test
    fun `пакет из будущей версии отвергается с понятной причиной`() {
        val future = RegionPackCodec.encode(pack()).replace("region-pack/3", "region-pack/4")

        val error = RegionPackCodec.decode(future).exceptionOrNull()!!

        assertTrue(error.message!!.contains("новой версией"))
    }

    @Test
    fun `незнакомые поля не мешают чтению`() {
        // Пакет, собранный приложением с новыми возможностями, должен
        // читаться, пока схема того же поколения.
        val extended = RegionPackCodec.encode(pack())
            .replaceFirst("\"spots\"", "\"catches\": [], \"spots\"")

        assertTrue(RegionPackCodec.decode(extended).isSuccess)
    }

    @Test
    fun `пакет со старыми зонами читается без них`() {
        // Зоны из формата ушли, но у рыболовов на руках остались старые
        // пакеты: незнакомый раздел игнорируется, а точки приезжают как
        // приезжали.
        val withZones = RegionPackCodec.encode(pack()).replaceFirst(
            "\"spots\"",
            "\"zones\": [ { \"id\": \"zone-1\", \"name\": \"Залив\", \"kind\": \"WATER\", " +
                "\"outline\": [ { \"lat\": 55.79, \"lon\": 37.61 } ] } ], \"spots\""
        )

        val restored = RegionPackCodec.decode(withZones).getOrThrow()

        assertEquals(1, restored.spots.size)
        assertEquals("Мысок", restored.spots.first().name)
    }
}
