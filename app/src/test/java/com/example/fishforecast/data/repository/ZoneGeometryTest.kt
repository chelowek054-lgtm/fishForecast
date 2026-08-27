package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.entities.ZoneEntity
import com.example.fishforecast.data.local.entities.ZoneKind
import com.example.fishforecast.domain.share.GeoPoint
import com.example.fishforecast.domain.share.encodeOutline
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ZoneGeometryTest {

    /** Квадрат со стороной 0.1° — примерно залив в километр. */
    private val square = listOf(
        GeoPoint(55.70, 37.60),
        GeoPoint(55.80, 37.60),
        GeoPoint(55.80, 37.70),
        GeoPoint(55.70, 37.70)
    )

    private fun zone(name: String, outline: List<GeoPoint>) = ZoneEntity(
        uid = name,
        mapUid = "region-1",
        name = name,
        kind = ZoneKind.WATER.name,
        outline = outline.encodeOutline()
    )

    @Test
    fun `точка внутри контура распознаётся`() {
        assertTrue(square.containsPoint(GeoPoint(55.75, 37.65)))
    }

    @Test
    fun `точка снаружи контура не считается своей`() {
        assertFalse(square.containsPoint(GeoPoint(55.75, 37.80)))
        assertFalse(square.containsPoint(GeoPoint(55.60, 37.65)))
    }

    @Test
    fun `ломаная из двух точек площади не задаёт`() {
        val line = listOf(GeoPoint(55.70, 37.60), GeoPoint(55.80, 37.60))

        assertFalse(line.containsPoint(GeoPoint(55.75, 37.60)))
    }

    @Test
    fun `контур внутри зоны становится её сектором`() {
        val zones = listOf(zone("Северный залив", square))
        val inside = listOf(
            GeoPoint(55.74, 37.64),
            GeoPoint(55.76, 37.64),
            GeoPoint(55.76, 37.66)
        )

        assertEquals("Северный залив", zones.enclosing(inside)?.name)
    }

    @Test
    fun `контур, вылезающий за зону, остаётся самостоятельным`() {
        val zones = listOf(zone("Северный залив", square))
        val overlapping = listOf(
            GeoPoint(55.75, 37.65),
            GeoPoint(55.85, 37.65),
            GeoPoint(55.85, 37.75)
        )

        assertNull(zones.enclosing(overlapping))
    }

    @Test
    fun `на пустой карте вкладывать некуда`() {
        assertNull(emptyList<ZoneEntity>().enclosing(square))
    }
}
