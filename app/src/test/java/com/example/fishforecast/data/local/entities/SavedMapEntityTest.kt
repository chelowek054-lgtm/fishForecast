package com.example.fishforecast.data.local.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedMapEntityTest {

    private val map = SavedMapEntity(
        id = 1,
        name = "Озеро",
        offlineRegionId = 1L,
        north = 56.0,
        south = 55.0,
        east = 38.0,
        west = 37.0,
        minZoom = 12.0,
        maxZoom = 14.0
    )

    @Test
    fun `центр берётся из середины границ`() {
        assertEquals(55.5, map.centerLatitude, 0.0001)
        assertEquals(37.5, map.centerLongitude, 0.0001)
    }

    @Test
    fun `точка внутри границ принадлежит карте`() {
        assertTrue(map.contains(55.5, 37.5))
        assertTrue("границы включаются", map.contains(56.0, 38.0))
    }

    @Test
    fun `точка за границами карте не принадлежит`() {
        assertFalse(map.contains(54.9, 37.5))
        assertFalse(map.contains(55.5, 39.0))
    }

    @Test
    fun `градус долготы короче градуса широты вдали от экватора`() {
        // На 55-й параллели градус долготы примерно вдвое короче.
        assertTrue(
            "ширина ${map.widthKm} должна быть меньше высоты ${map.heightKm}",
            map.widthKm < map.heightKm
        )
        assertEquals(111.32, map.heightKm, 0.01)
    }
}
