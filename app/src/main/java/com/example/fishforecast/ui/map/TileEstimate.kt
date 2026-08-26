package com.example.fishforecast.ui.map

import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.tan

/**
 * Сколько тайлов придётся скачать для области.
 *
 * MapLibre отказывается качать больше [TILE_LIMIT] тайлов, но сообщает об
 * этом только в процессе загрузки. Оценка заранее позволяет предупредить
 * рыболова до того, как он выберет заведомо неподъёмный район.
 */
fun estimateTileCount(
    north: Double,
    south: Double,
    east: Double,
    west: Double,
    minZoom: Int,
    maxZoom: Int
): Long {
    if (north <= south || east <= west || minZoom > maxZoom) return 0

    return (minZoom..maxZoom).sumOf { zoom ->
        val scale = 2.0.pow(zoom)

        val left = floor(lonToTile(west, scale))
        val right = floor(lonToTile(east, scale))
        // Ось Y растёт к югу, поэтому север даёт меньший индекс.
        val top = floor(latToTile(north, scale))
        val bottom = floor(latToTile(south, scale))

        ((right - left + 1) * (bottom - top + 1)).toLong()
    }
}

private fun lonToTile(longitude: Double, scale: Double): Double =
    (longitude + 180.0) / 360.0 * scale

private fun latToTile(latitude: Double, scale: Double): Double {
    // Меркатор не определён на полюсах, поэтому широта зажимается.
    val safe = latitude.coerceIn(-MAX_MERCATOR_LATITUDE, MAX_MERCATOR_LATITUDE)
    val radians = safe * PI / 180.0
    return (1.0 - asinh(tan(radians)) / PI) / 2.0 * scale
}

/** Предел MapLibre для одной офлайн-области. */
const val TILE_LIMIT = 6000L

private const val MAX_MERCATOR_LATITUDE = 85.05
