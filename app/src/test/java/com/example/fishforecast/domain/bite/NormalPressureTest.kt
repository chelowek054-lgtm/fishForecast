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
    fun `посчитанная норма идёт в дело, когда рыболов ничего не задал`() {
        val normal = resolveNormalPressure(
            mapNormalMmHg = null,
            spotNormalMmHg = null,
            baselineMmHg = 745.8
        )

        assertEquals(745.8, normal!!, 0.0001)
    }

    @Test
    fun `своё значение старше расчёта`() {
        val normal = resolveNormalPressure(
            mapNormalMmHg = 750.0,
            spotNormalMmHg = null,
            baselineMmHg = 745.8
        )

        assertEquals(750.0, normal!!, 0.0001)
    }

    @Test
    fun `без обеих норм ориентир не задан`() {
        // Тогда CalculateFishActivityUseCase возьмёт середину диапазона рыбы.
        assertNull(resolveNormalPressure(mapNormalMmHg = null, spotNormalMmHg = null))
    }

    @Test
    fun `среднее по наблюдениям считается в миллиметрах`() {
        // Ряд ровно из 1013.25 гПа — это 760 мм рт. ст. по определению.
        val samples = List(MIN_SAMPLE_HOURS) { 1013.25 }

        assertEquals(760.0, averagePressureMmHg(samples)!!, 0.1)
    }

    @Test
    fun `пропуски в ряду не мешают, если наблюдений хватает`() {
        val samples = List(MIN_SAMPLE_HOURS) { 994.4 } + List(500) { null }

        assertEquals(745.9, averagePressureMmHg(samples)!!, 0.2)
    }

    @Test
    fun `короткий ряд усредняет погоду, а не норму`() {
        assertNull(averagePressureMmHg(List(48) { 1000.0 }))
    }

    @Test
    fun `высота места даёт норму без всякой истории`() {
        // Под Москвой 152 м: среднее за семьдесят суток наблюдений — 745.8.
        assertEquals(746.0, standardPressureMmHg(152.0), 1.0)
        assertEquals(760.0, standardPressureMmHg(0.0), 0.5)
    }

    @Test
    fun `выше в горы — ниже давление`() {
        assertEquals(true, standardPressureMmHg(1500.0) < standardPressureMmHg(200.0))
    }
}
