package com.example.fishforecast.data.local.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedMapEntityTest {

    /** Прямоугольник вокруг Москвы: примерно 0.2° по широте и долготе. */
    private val map = SavedMapEntity(
        id = 1,
        name = "Район",
        offlineRegionId = 1,
        north = 55.85,
        south = 55.65,
        east = 37.75,
        west = 37.55,
        minZoom = 10.0,
        maxZoom = 14.0
    )

    @Test
    fun `центр района лежит посередине границ`() {
        // По центру запрашивается погода, поэтому промах сместил бы прогноз.
        assertEquals(55.75, map.centerLatitude, 0.0001)
        assertEquals(37.65, map.centerLongitude, 0.0001)
    }

    @Test
    fun `точка внутри границ принадлежит карте`() {
        assertTrue(map.contains(latitude = 55.75, longitude = 37.65))
        assertTrue("угол тоже внутри", map.contains(latitude = 55.85, longitude = 37.75))
    }

    @Test
    fun `точка за границами карте не принадлежит`() {
        assertFalse("севернее", map.contains(latitude = 55.95, longitude = 37.65))
        assertFalse("западнее", map.contains(latitude = 55.75, longitude = 37.40))
    }

    @Test
    fun `охват по широте считается в километрах`() {
        // 0.2° широты — это чуть больше 22 км в любой точке планеты.
        assertEquals(22.3, map.heightKm, 0.3)
    }

    @Test
    fun `градус долготы короче градуса широты вдали от экватора`() {
        // Меридианы сходятся к полюсам, поэтому одинаковый размах в градусах
        // даёт разную ширину: на широте Москвы примерно вдвое меньше.
        val equator = map.copy(north = 0.1, south = -0.1)

        assertTrue(
            "у Москвы ${map.widthKm} км должно быть заметно меньше, чем ${equator.widthKm} км на экваторе",
            map.widthKm < equator.widthKm * 0.7
        )
    }
}
