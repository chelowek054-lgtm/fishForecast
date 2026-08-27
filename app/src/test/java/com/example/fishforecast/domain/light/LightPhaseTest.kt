package com.example.fishforecast.domain.light

import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.domain.fish.Guild
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class LightPhaseTest {

    /** Летний день под Москвой: рассвет в 5:11, закат в 20:30. */
    private val sun = DailySunEntity(
        mapId = 1,
        date = "2026-08-27",
        sunrise = "2026-08-27T05:11",
        sunset = "2026-08-27T20:30"
    )

    private fun phaseAt(hour: Int, minute: Int = 0): LightPhase? =
        lightPhaseAt(LocalDateTime.of(2026, 8, 27, hour, minute), sun)

    @Test
    fun `зори привязаны к солнцу, а не к часам`() {
        assertEquals(LightPhase.DAWN, phaseAt(5, 0))
        assertEquals(LightPhase.DAWN, phaseAt(5, 40))
        assertEquals(LightPhase.DUSK, phaseAt(20, 15))
    }

    @Test
    fun `ночь до рассвета и после заката`() {
        assertEquals(LightPhase.NIGHT, phaseAt(2))
        assertEquals(LightPhase.NIGHT, phaseAt(23))
    }

    @Test
    fun `утро переходит в день, день в вечер`() {
        assertEquals(LightPhase.MORNING, phaseAt(7))
        assertEquals(LightPhase.DAY, phaseAt(13))
        assertEquals(LightPhase.EVENING, phaseAt(19))
    }

    @Test
    fun `без данных о солнце фаза не выдумывается`() {
        assertNull(lightPhaseAt(LocalDateTime.of(2026, 8, 27, 13, 0), null))
    }

    @Test
    fun `у хищника зори резче, чем у мирной рыбы`() {
        val predatorDawn = lightActivity(LightPhase.DAWN, emptyMap(), Guild.PREDATOR)
        val predatorDay = lightActivity(LightPhase.DAY, emptyMap(), Guild.PREDATOR)
        val peacefulDawn = lightActivity(LightPhase.DAWN, emptyMap(), Guild.PEACEFUL)
        val peacefulDay = lightActivity(LightPhase.DAY, emptyMap(), Guild.PEACEFUL)

        assertTrue(
            "у хищника разрыв между зорёй и полуднем больше",
            predatorDawn - predatorDay > peacefulDawn - peacefulDay
        )
    }

    @Test
    fun `свой профиль вида старше умолчания гильдии`() {
        // Налим — хищник, но ночной: правило про зори к нему не применимо.
        val burbot = mapOf("night" to 1.0, "dawn" to 0.6, "day" to 0.2)

        assertEquals(1.0, lightActivity(LightPhase.NIGHT, burbot, Guild.PREDATOR), 0.001)
        assertEquals(0.2, lightActivity(LightPhase.DAY, burbot, Guild.PREDATOR), 0.001)
    }

    @Test
    fun `пробел в чужом профиле закрывается умолчанием`() {
        // Справочник с сервера может не описать все фазы — расчёт не должен
        // падать и не должен считать такую фазу нулевой.
        val partial = mapOf("night" to 0.9)

        val evening = lightActivity(LightPhase.EVENING, partial, Guild.PREDATOR)

        assertEquals(defaultLightActivity(Guild.PREDATOR).getValue("evening"), evening, 0.001)
    }
}
