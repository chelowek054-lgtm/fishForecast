package com.example.fishforecast.domain.season

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.domain.water.WaterHour
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

/**
 * Фаза сезона.
 *
 * Главное, что здесь проверяется, — что решает вода и её направление, а не
 * календарь. Четырнадцать градусов на подъёме и те же четырнадцать на спаде
 * должны давать разные фазы: в первом случае рыба идёт к нересту, во втором
 * нагуливает перед зимой.
 */
class SeasonPhaseTest {

    private val now = LocalDateTime.parse("2026-05-20T12:00")

    private val carp = FishEntity(
        id = 1,
        name = "Карп",
        guild = "peaceful",
        optMinTemp = 18f,
        optMaxTemp = 24f,
        absMinTemp = 3f,
        absMaxTemp = 32f,
        spawnTempMinC = 18f,
        spawnTempMaxC = 22f,
        feedStartC = 10f,
        postSpawnRecoveryDays = 10
    )

    private val burbot = FishEntity(
        id = 2,
        name = "Налим",
        guild = "predator",
        optMinTemp = 8f,
        optMaxTemp = 13f,
        absMinTemp = 0f,
        absMaxTemp = 20f,
        spawnTempMinC = 1f,
        spawnTempMaxC = 4f,
        dormantAboveC = 15f
    )

    /**
     * Ход воды за [days] суток: от [from] до [to] линейно, час за часом.
     * Последний час — ровно `now`.
     */
    private fun history(from: Double, to: Double, days: Int = 4): List<WaterHour> {
        val hours = days * 24
        val start = now.minusHours(hours.toLong() - 1)
        return (0 until hours).map { index ->
            WaterHour(
                time = start.plusHours(index.toLong()).toString(),
                temperature = from + (to - from) * index / (hours - 1)
            )
        }
    }

    @Test
    fun `вода растёт и подходит к нересту снизу — жор`() {
        val state = seasonPhaseOf(carp, history(from = 13.0, to = 16.0), now)

        assertEquals(SeasonPhase.PRE_SPAWN, state!!.phase)
        assertEquals(16.0, state.waterC, 0.01)
        // Окно направления — трое суток, а история длиннее: ход считается по
        // окну, а не по всей истории.
        assertEquals(3, state.overDays)
        assertEquals(2.27, state.driftC, 0.01)
    }

    @Test
    fun `в нерестовой полосе на подъёме — нерест`() {
        val state = seasonPhaseOf(carp, history(from = 17.0, to = 20.0), now)

        assertEquals(SeasonPhase.SPAWN, state!!.phase)
    }

    @Test
    fun `та же полоса на спаде нерестом не является`() {
        // Осенью вода проходит те же двадцать градусов вниз. Икры там нет.
        val state = seasonPhaseOf(carp, history(from = 23.0, to = 20.0), now)

        assertEquals(SeasonPhase.AUTUMN, state!!.phase)
    }

    @Test
    fun `сразу после нереста рыба отходит`() {
        // Вода вышла из полосы вверх меньше суток назад.
        val state = seasonPhaseOf(carp, history(from = 20.0, to = 23.0), now)

        assertEquals(SeasonPhase.POST_SPAWN, state!!.phase)
        assertEquals(1, state.daysSinceSpawn)
    }

    @Test
    fun `когда нерест не виден в истории, фаза обычная`() {
        // История хранится неделю: если полосы в ней нет, догадываться не о чем.
        val state = seasonPhaseOf(carp, history(from = 25.0, to = 26.0), now)

        assertEquals(SeasonPhase.HEAT, state!!.phase)
        assertNull(state.daysSinceSpawn)
    }

    @Test
    fun `ниже порога кормления — зимнее стояние`() {
        val state = seasonPhaseOf(carp, history(from = 8.0, to = 9.0), now)

        assertEquals(SeasonPhase.WINTER, state!!.phase)
    }

    @Test
    fun `налим летом спит, и это сильнее всего остального`() {
        val state = seasonPhaseOf(burbot, history(from = 16.0, to = 18.0), now)

        assertEquals(SeasonPhase.DORMANT, state!!.phase)
    }

    @Test
    fun `налим в холодной воде на своём нересте`() {
        val state = seasonPhaseOf(burbot, history(from = 1.0, to = 3.0), now)

        assertEquals(SeasonPhase.SPAWN, state!!.phase)
    }

    @Test
    fun `ровная вода — ни жора, ни нагула`() {
        // Ход меньше полуградуса за трое суток считается шумом.
        val state = seasonPhaseOf(carp, history(from = 20.0, to = 20.1), now)

        assertEquals(SeasonPhase.SUMMER, state!!.phase)
    }

    @Test
    fun `без суток истории фазы нет`() {
        // Направление по трём часам не определить, а выдумывать его нельзя.
        val short = history(from = 15.0, to = 16.0).takeLast(3)

        assertNull(seasonPhaseOf(carp, short, now))
    }

    @Test
    fun `часы из будущего в расчёт не идут`() {
        // История уходит вперёд вместе с прогнозом; сезон судят по прошедшему.
        val withFuture = history(from = 13.0, to = 16.0) +
            WaterHour(time = now.plusHours(6).toString(), temperature = 30.0)

        assertEquals(SeasonPhase.PRE_SPAWN, seasonPhaseOf(carp, withFuture, now)!!.phase)
    }
}
