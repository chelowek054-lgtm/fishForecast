package com.example.fishforecast.domain.share

import com.example.fishforecast.data.local.entities.FishingSpotEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxWriterTest {

    private fun spot(
        name: String = "Точка",
        note: String = "",
        fishId: Int? = null
    ) = FishingSpotEntity(
        id = 1,
        name = name,
        latitude = 55.74911,
        longitude = 37.62277,
        fishId = fishId,
        note = note
    )

    @Test
    fun `координаты попадают в атрибуты wpt`() {
        val gpx = GpxWriter.write(listOf(spot()))

        assertTrue(gpx.contains("""<wpt lat="55.74911" lon="37.62277">"""))
    }

    @Test
    fun `спецсимволы в названии не ломают XML`() {
        val gpx = GpxWriter.write(listOf(spot(name = """Щука & "омут" <тайное>""")))

        assertTrue(gpx.contains("<name>Щука &amp; &quot;омут&quot; &lt;тайное&gt;</name>"))
        assertTrue(!gpx.contains("<тайное>"))
    }

    @Test
    fun `привязанная рыба и заметка попадают в описание`() {
        val gpx = GpxWriter.write(
            spots = listOf(spot(note = "Ловить с утра", fishId = 7)),
            fishNameById = mapOf(7 to "Щука")
        )

        assertTrue(gpx.contains("<desc>Здесь берёт: Щука. Ловить с утра</desc>"))
    }

    @Test
    fun `пустое описание не попадает в файл`() {
        val gpx = GpxWriter.write(listOf(spot()))

        assertTrue(!gpx.contains("<desc>"))
    }

    @Test
    fun `пустой список даёт валидный gpx без точек`() {
        val gpx = GpxWriter.write(emptyList())

        assertTrue(gpx.startsWith("""<?xml version="1.0" encoding="UTF-8"?>"""))
        assertTrue(gpx.trimEnd().endsWith("</gpx>"))
        assertEquals(0, gpx.split("<wpt").size - 1)
    }
}
