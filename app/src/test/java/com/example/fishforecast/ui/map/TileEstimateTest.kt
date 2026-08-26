package com.example.fishforecast.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileEstimateTest {

    @Test
    fun `небольшой водоём укладывается в лимит`() {
        // Примерно 2 × 2 км под Москвой, масштабы 12–14.
        val tiles = estimateTileCount(
            north = 55.76, south = 55.74, east = 37.66, west = 37.64,
            minZoom = 12, maxZoom = 14
        )

        assertTrue("получилось $tiles тайлов", tiles in 1..TILE_LIMIT)
    }

    @Test
    fun `весь мир в лимит не помещается`() {
        val tiles = estimateTileCount(
            north = 85.0, south = -85.0, east = 179.0, west = -179.0,
            minZoom = 8, maxZoom = 14
        )

        assertTrue("ожидался перебор, получилось $tiles", tiles > TILE_LIMIT)
    }

    @Test
    fun `на нулевом масштабе весь мир это один тайл`() {
        val tiles = estimateTileCount(
            north = 80.0, south = -80.0, east = 179.0, west = -179.0,
            minZoom = 0, maxZoom = 0
        )

        assertEquals(1L, tiles)
    }

    @Test
    fun `каждый следующий масштаб добавляет тайлов`() {
        val narrow = estimateTileCount(55.76, 55.74, 37.66, 37.64, minZoom = 12, maxZoom = 12)
        val wide = estimateTileCount(55.76, 55.74, 37.66, 37.64, minZoom = 12, maxZoom = 14)

        assertTrue("$wide должно быть больше $narrow", wide > narrow)
    }

    @Test
    fun `перевёрнутые границы дают ноль вместо мусора`() {
        assertEquals(0L, estimateTileCount(55.74, 55.76, 37.64, 37.66, 12, 14))
        assertEquals(0L, estimateTileCount(55.76, 55.74, 37.66, 37.64, minZoom = 14, maxZoom = 12))
    }
}
