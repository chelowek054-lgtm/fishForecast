package com.example.fishforecast.ui.maps

import org.junit.Assert.assertEquals
import org.junit.Test

class MapSizeFormatTest {

    @Test
    fun `район меньше километра показывается в метрах`() {
        // Раньше такой район выглядел как «0 км», а рыболов сохраняет один
        // водоём — это самый частый случай.
        assertEquals("800 м", formatDistance(0.8))
        assertEquals("120 м", formatDistance(0.12))
    }

    @Test
    fun `небольшой район показывается с десятыми`() {
        assertEquals("2", formatDistance(2.54).take(1))
        assertEquals("км", formatDistance(2.54).takeLast(2))
    }

    @Test
    fun `крупный район округляется до километров`() {
        assertEquals("12 км", formatDistance(12.3))
        assertEquals("45 км", formatDistance(45.0))
    }

    @Test
    fun `отрицательный размах не ломает подпись`() {
        // Границы могут прийти в обратном порядке — знак тут не имеет смысла.
        assertEquals("800 м", formatDistance(-0.8))
    }
}
