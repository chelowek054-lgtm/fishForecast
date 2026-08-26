package com.example.fishforecast.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapConfigTest {

    @Test
    fun `при сильном приближении масштабы не переворачиваются`() {
        // Рыболов разглядывал водоём на 17-м зуме и нажал «Сохранить».
        // Раньше это давало minZoom 17 > maxZoom 14 и java.lang.Error.
        val range = MapConfig.offlineZoomRange(currentZoom = 17.0)

        assertTrue("minZoom ${range.start} > maxZoom ${range.endInclusive}", range.start <= range.endInclusive)
        assertEquals(MapConfig.MAX_OFFLINE_ZOOM, range.start, 0.0001)
    }

    @Test
    fun `слишком мелкий масштаб поднимается до нижней границы`() {
        val range = MapConfig.offlineZoomRange(currentZoom = 3.0)

        assertEquals(MapConfig.MIN_OFFLINE_ZOOM, range.start, 0.0001)
        assertEquals(MapConfig.MAX_OFFLINE_ZOOM, range.endInclusive, 0.0001)
    }

    @Test
    fun `обычный масштаб берётся как есть`() {
        val range = MapConfig.offlineZoomRange(currentZoom = 11.5)

        assertEquals(11.5, range.start, 0.0001)
        assertEquals(MapConfig.MAX_OFFLINE_ZOOM, range.endInclusive, 0.0001)
    }

    @Test
    fun `диапазон корректен на всём разумном промежутке зумов`() {
        var zoom = 0.0
        while (zoom <= 22.0) {
            val range = MapConfig.offlineZoomRange(zoom)
            assertTrue(
                "зум $zoom дал ${range.start}..${range.endInclusive}",
                range.start <= range.endInclusive
            )
            zoom += 0.5
        }
    }
}
