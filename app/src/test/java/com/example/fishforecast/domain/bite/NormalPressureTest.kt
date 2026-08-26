package com.example.fishforecast.domain.bite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalPressureTest {

    @Test
    fun `норма точки уточняет норму карты`() {
        val normal = resolveNormalPressure(mapNormalMmHg = 750.0, spotNormalMmHg = 744.0)

        assertEquals(744.0, normal!!, 0.0001)
    }

    @Test
    fun `без нормы у точки берётся норма карты`() {
        val normal = resolveNormalPressure(mapNormalMmHg = 750.0, spotNormalMmHg = null)

        assertEquals(750.0, normal!!, 0.0001)
    }

    @Test
    fun `без обеих норм ориентир не задан`() {
        // Тогда CalculateFishActivityUseCase возьмёт середину диапазона рыбы.
        assertNull(resolveNormalPressure(mapNormalMmHg = null, spotNormalMmHg = null))
    }
}
