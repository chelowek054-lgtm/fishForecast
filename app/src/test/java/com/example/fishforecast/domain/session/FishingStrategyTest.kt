package com.example.fishforecast.domain.session

import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.session.HourContext
import com.example.fishforecast.domain.fish.FishCatalogCodec
import com.example.fishforecast.domain.fish.toEntity
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import com.example.fishforecast.domain.light.LightPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FishingStrategyTest {

    private val knowledge = KnowledgeCodec
        .decode(File("src/main/assets/knowledge.json").readText())
        .getOrThrow()

    private val catalog = FishCatalogCodec
        .decode(File("src/main/assets/initial_fish.json").readText())
        .getOrThrow()

    private val carp = catalog.fish.first { it.id == "carp" }.toEntity()
    private val pike = catalog.fish.first { it.id == "pike" }.toEntity()

    private fun conditions(
        shallow: Double,
        deep: Double = shallow - 2,
        oxygen: Double = 8.0,
        phase: LightPhase = LightPhase.EVENING,
        rain: Double = 0.0,
        scoreShallow: Int = 70,
        scoreDeep: Int = 60
    ) = SessionConditions(
        hour = null,
        waterShallowC = shallow,
        waterDeepC = deep,
        oxygenMgL = oxygen,
        lightPhase = phase,
        hours = (0 until 6).map { index ->
            HourContext(
                time = "2026-08-28T%02d:00".format(index + 18),
                phase = phase,
                shallowC = shallow,
                deepC = deep,
                oxygenMgL = oxygen,
                scoreShallow = scoreShallow,
                scoreDeep = scoreDeep
            )
        },
        rainLastDayMm = rain
    )

    private fun carpPlan(
        conditions: SessionConditions,
        methodId: String? = "feeder_flat"
    ) = buildStrategy(
        input = SessionPlanInput(fish = carp, methodId = methodId),
        conditions = conditions,
        knowledge = knowledge
    )

    @Test
    fun `в прогретой воде с бедным кислородом советует толщу, а не дно`() {
        // Тот самый вечер: карп гуляет поверху, а флэт лежит на дне.
        val plan = carpPlan(conditions(shallow = 28.0, oxygen = 5.2))

        assertEquals("Толща воды", plan.horizon.value)
        assertTrue(
            "должно быть сказано, что снасть работает не там: ${plan.horizon.reason}",
            plan.horizon.reason.contains("дну")
        )
    }

    @Test
    fun `в комфортной воде остаётся дно`() {
        val plan = carpPlan(conditions(shallow = 22.0, oxygen = 8.5))

        assertEquals("Дно", plan.horizon.value)
    }

    @Test
    fun `в жару прикормки советует меньше, чем справочник`() {
        // Справочник для тёплой воды говорит «обильно», но в духоте это
        // собирает мелочь и поднимает рыбу над кормом.
        val normal = carpPlan(conditions(shallow = 22.0, oxygen = 8.5)).groundbait!!
        val heat = carpPlan(conditions(shallow = 27.0, oxygen = 5.2)).groundbait!!

        assertTrue("в комфорте кормим обильно: ${normal.value}", normal.value.contains("обильно"))
        assertTrue("в духоте — меньше: ${heat.value}", heat.value.contains("меньше"))
    }

    @Test
    fun `всегда напоминает промерить точку`() {
        val plan = carpPlan(conditions(shallow = 22.0))

        assertTrue(
            "нужно напоминание о промере: ${plan.warnings}",
            plan.warnings.any { it.contains("промер") }
        )
    }

    @Test
    fun `место выбирается само по лучшему слою`() {
        val toShore = carpPlan(conditions(shallow = 22.0, scoreShallow = 80, scoreDeep = 60))
        val toDeep = carpPlan(conditions(shallow = 28.0, scoreShallow = 55, scoreDeep = 80))

        assertTrue("к берегу: ${toShore.place.value}", toShore.place.value.contains("берегу"))
        assertTrue("на глубину: ${toDeep.place.value}", toDeep.place.value.contains("глубину"))
    }

    @Test
    fun `раскладка суток склеивает соседние часы с одинаковым советом`() {
        val plan = carpPlan(conditions(shallow = 22.0))

        assertTrue("раскладка не пустая", plan.day.isNotEmpty())
        assertTrue(
            "шесть одинаковых часов должны склеиться в один отрезок: ${plan.day.size}",
            plan.day.size == 1
        )
        assertEquals("18:00", plan.day.first().fromTime)
        assertEquals("23:00", plan.day.first().toTime)
    }

    @Test
    fun `хищнику советует приманку, а не прикормку`() {
        val plan = buildStrategy(
            input = SessionPlanInput(pike, "spinning"),
            conditions = conditions(shallow = 16.0),
            knowledge = knowledge
        )

        assertNotNull(plan.bait)
        assertEquals("Приманка", plan.bait!!.title)
        assertEquals("Не нужна", plan.groundbait?.value)
    }

    @Test
    fun `в мутной воде после ливня приманка ярче и крупнее`() {
        val clear = buildStrategy(
            input = SessionPlanInput(pike, "spinning"),
            conditions = conditions(shallow = 16.0, rain = 0.0, phase = LightPhase.DAY),
            knowledge = knowledge
        ).bait!!

        val muddy = buildStrategy(
            input = SessionPlanInput(pike, "spinning"),
            conditions = conditions(shallow = 16.0, rain = 20.0, phase = LightPhase.DAY),
            knowledge = knowledge
        ).bait!!

        assertTrue("в прозрачной воде — натуральное: ${clear.value}", clear.value.contains("натур"))
        assertTrue("в мути — кислотное: ${muddy.value}", muddy.value.contains("кислот"))
        assertTrue("в мути крупнее: ${muddy.value}", muddy.value.contains("крупнее"))
    }

    @Test
    fun `в холодной воде подача медленная`() {
        val plan = buildStrategy(
            input = SessionPlanInput(pike, "spinning"),
            conditions = conditions(shallow = 8.0, phase = LightPhase.DAY),
            knowledge = knowledge
        )

        assertTrue(
            "должна быть медленная подача: ${plan.bait?.reason}",
            plan.bait!!.reason.contains("медленн")
        )
    }

    @Test
    fun `план подсказывает, что искать на месте`() {
        val plan = carpPlan(conditions(shallow = 22.0))

        assertTrue("у карпа есть любимые структуры", plan.lookFor.isNotEmpty())
        assertTrue(plan.lookFor.any { it.id == "reeds" || it.id == "bay" })
    }

    @Test
    fun `вода теплее предела вида — честно советует ехать за другим`() {
        val plan = carpPlan(conditions(shallow = 32.0, oxygen = 4.0))

        assertTrue(
            "нужно сказать прямо: ${plan.warnings}",
            plan.warnings.any { it.contains("за кем-то другим") }
        )
    }
}
