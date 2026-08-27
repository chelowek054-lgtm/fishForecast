package com.example.fishforecast.domain.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class WindTest {

    @Test
    fun `румб определяется по секторам`() {
        assertEquals("С", windDirectionLabel(0.0))
        assertEquals("В", windDirectionLabel(90.0))
        assertEquals("Ю", windDirectionLabel(180.0))
        assertEquals("З", windDirectionLabel(270.0))
        assertEquals("СЗ", windDirectionLabel(315.0))
    }

    @Test
    fun `граница оборота остаётся северной`() {
        assertEquals("С", windDirectionLabel(359.0))
        assertEquals("С", windDirectionLabel(360.0))
        assertEquals("С", windDirectionLabel(-10.0))
    }

    @Test
    fun `стрелка показывает куда летит ветер`() {
        assertEquals(180f, windArrowRotation(0.0), 0.001f)
        assertEquals(0f, windArrowRotation(180.0), 0.001f)
    }

    @Test
    fun `скорость переводится в метры в секунду`() {
        assertEquals(10.0, 36.0.kmhToMs(), 0.001)
    }
}
