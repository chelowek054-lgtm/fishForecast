package com.example.fishforecast.domain.share

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.SectorEntity
import com.example.fishforecast.data.local.entities.SpotPlacement
import com.example.fishforecast.data.local.entities.ZoneEntity
import com.example.fishforecast.data.local.entities.ZoneKind
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

    private val zone = ZoneEntity(
        uid = "zone-1",
        mapUid = "region-1",
        name = "Северный залив",
        kind = ZoneKind.WATER.name,
        outline = listOf(
            GeoPoint(55.79, 37.61),
            GeoPoint(55.79, 37.63),
            GeoPoint(55.77, 37.62)
        ).encodeOutline()
    )

    private val sector = SectorEntity(
        uid = "sector-1",
        zoneUid = "zone-1",
        name = "3",
        outline = listOf(
            GeoPoint(55.785, 37.615),
            GeoPoint(55.785, 37.62),
            GeoPoint(55.78, 37.617)
        ).encodeOutline()
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
        zoneUid = "zone-1",
        sectorUid = "sector-1"
    )

    private fun pack() = RegionPackCodec.build(
        map = map,
        zones = listOf(zone),
        sectors = listOf(sector),
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
        assertEquals(1, restored.zones.size)
        assertEquals(1, restored.zones.first().sectors.size)
        assertEquals(3, restored.zones.first().outline.size)
        assertEquals("SHORE", restored.spots.first().placement)
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
    fun `контур зоны не теряет вершины при обмене`() {
        val contents = RegionPackCodec.toEntities(pack())
        val outline = contents.zones.first().outline.decodeOutline()

        assertEquals(3, outline.size)
        assertEquals(55.79, outline.first().latitude, 0.000001)
        assertEquals(37.61, outline.first().longitude, 0.000001)
        assertTrue(outline.isPolygon())
    }

    @Test
    fun `чужой формат отвергается, а не читается наполовину`() {
        val alien = """{"schema":"someapp/1","id":"x","createdAt":"","region":{}}"""

        assertTrue(RegionPackCodec.decode(alien).isFailure)
    }

    @Test
    fun `пакет из будущей версии отвергается с понятной причиной`() {
        val future = RegionPackCodec.encode(pack()).replace("region-pack/2", "region-pack/3")

        val error = RegionPackCodec.decode(future).exceptionOrNull()!!

        assertTrue(error.message!!.contains("новой версией"))
    }

    @Test
    fun `незнакомые поля не мешают чтению`() {
        // Пакет, собранный приложением с новыми возможностями, должен
        // читаться, пока схема того же поколения.
        val extended = RegionPackCodec.encode(pack())
            .replaceFirst("\"zones\"", "\"catches\": [], \"zones\"")

        assertTrue(RegionPackCodec.decode(extended).isSuccess)
    }
}
