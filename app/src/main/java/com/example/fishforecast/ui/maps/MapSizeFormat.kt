package com.example.fishforecast.ui.maps

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Размер района словами.
 *
 * Рыболов сохраняет один водоём, а это обычно меньше километра — округление
 * до целых километров превращало такой район в «0 км». Поэтому мелкие
 * расстояния показываются в метрах, средние — с десятыми долями.
 */
internal fun formatDistance(km: Double): String {
    val value = abs(km)

    return when {
        value < 1.0 -> "${(value * 1000).roundToInt()} м"
        value < 10.0 -> "%.1f км".format(value)
        else -> "${value.roundToInt()} км"
    }
}
