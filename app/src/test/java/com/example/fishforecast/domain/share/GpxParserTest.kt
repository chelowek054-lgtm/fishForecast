package com.example.fishforecast.domain.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxParserTest {

    private fun parse(gpx: String) = GpxParser.parse(gpx.byteInputStream())

    @Test
    fun `читает координаты имя и описание`() {
        val spots = parse(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" xmlns="http://www.topografix.com/GPX/1/1">
              <wpt lat="55.74911" lon="37.62277">
                <name>Омут</name>
                <desc>Здесь берёт: Щука</desc>
              </wpt>
            </gpx>
            """.trimIndent()
        )

        assertEquals(1, spots.size)
        assertEquals("Омут", spots[0].name)
        assertEquals("Здесь берёт: Щука", spots[0].note)
        assertEquals(55.74911, spots[0].latitude, 0.000001)
        assertEquals(37.62277, spots[0].longitude, 0.000001)
    }

    @Test
    fun `точка без координат пропускается`() {
        val spots = parse(
            """
            <gpx version="1.1">
              <wpt><name>Без координат</name></wpt>
              <wpt lat="1.5" lon="2.5"><name>Хорошая</name></wpt>
            </gpx>
            """.trimIndent()
        )

        assertEquals(1, spots.size)
        assertEquals("Хорошая", spots[0].name)
    }

    @Test
    fun `точка без имени получает имя по умолчанию`() {
        val spots = parse("""<gpx><wpt lat="1" lon="2"/></gpx>""")

        assertEquals("Импортированная точка", spots[0].name)
        assertEquals("", spots[0].note)
    }

    @Test
    fun `читает файл сделанный своим же writer`() {
        val original = GpxWriter.write(
            spots = listOf(
                com.example.fishforecast.data.local.entities.FishingSpotEntity(
                    name = """Щука & "омут"""",
                    latitude = 10.5,
                    longitude = 20.25,
                    note = "Заметка"
                )
            )
        )

        val spots = parse(original)

        assertEquals(1, spots.size)
        assertEquals("""Щука & "омут"""", spots[0].name)
        assertEquals(10.5, spots[0].latitude, 0.000001)
    }

    @Test
    fun `битый файл бросает исключение а не молчит`() {
        val failed = runCatching { parse("это не xml") }.isFailure

        assertTrue(failed)
    }
}
