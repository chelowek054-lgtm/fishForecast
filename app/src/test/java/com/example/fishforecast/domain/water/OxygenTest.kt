package com.example.fishforecast.domain.water

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OxygenTest {

    @Test
    fun `насыщение совпадает с табличным`() {
        assertEquals(14.6, oxygenSaturationMgL(0.0), 0.15)
        assertEquals(11.3, oxygenSaturationMgL(10.0), 0.15)
        assertEquals(9.1, oxygenSaturationMgL(20.0), 0.15)
        assertEquals(8.2, oxygenSaturationMgL(25.0), 0.15)
        assertEquals(7.5, oxygenSaturationMgL(30.0), 0.15)
    }

    @Test
    fun `тёплая вода держит меньше кислорода`() {
        assertTrue(oxygenSaturationMgL(28.0) < oxygenSaturationMgL(18.0))
    }

    @Test
    fun `уровни отражают практику прудового хозяйства`() {
        assertEquals(OxygenLevel.RICH, oxygenLevel(oxygenSaturationMgL(12.0)))
        assertEquals(OxygenLevel.ENOUGH, oxygenLevel(6.0))
        assertEquals(OxygenLevel.LOW, oxygenLevel(3.5))
        assertEquals(OxygenLevel.CRITICAL, oxygenLevel(2.0))
    }
}
