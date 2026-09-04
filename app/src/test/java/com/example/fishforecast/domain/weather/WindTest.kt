package com.example.fishforecast.domain.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindTest {

    @Test
    fun `румб определяется по секторам`() {
        assertEquals("С", windDirectionLabel(0.0))
        assertEquals("В", windDirectionLabel(90.0))
        assertEquals("Ю", windDirectionLabel(180.0))
        assertEquals("З", windDirectionLabel(270.0))
        assertEquals("С-З", windDirectionLabel(315.0))
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

    @Test
    fun `поворот ветра считается по короткой дуге`() {
        // Через север ветер поворачивает на двадцать градусов, а не на триста сорок.
        assertEquals(20.0, windTurn(350.0, 10.0), 0.001)
        assertEquals(20.0, windTurn(10.0, 350.0), 0.001)
        assertEquals(0.0, windTurn(180.0, 180.0), 0.001)
        assertEquals(180.0, windTurn(45.0, 225.0), 0.001)
        assertEquals(90.0, windTurn(0.0, 270.0), 0.001)
    }

    @Test
    fun `северным считается сектор от северо-запада до северо-востока`() {
        assertTrue(isNortherlyWind(0.0))
        assertTrue(isNortherlyWind(350.0))
        assertTrue(isNortherlyWind(40.0))
        assertFalse(isNortherlyWind(90.0))
        assertFalse(isNortherlyWind(180.0))
        assertFalse(isNortherlyWind(300.0))
    }
}
