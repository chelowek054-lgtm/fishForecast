package com.example.fishforecast.domain.bite

import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import com.example.fishforecast.domain.knowledge.ObservationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDateTime

class ObservationContextTest {

    /** Тот самый словарь, который едет с приложением. */
    private val catalog = KnowledgeCodec
        .decode(File("src/main/assets/knowledge.json").readText())
        .getOrThrow()

    private val noon = LocalDateTime.parse("2026-09-04T12:00")

    private fun noted(id: String, at: LocalDateTime = noon) =
        ActiveObservation(type = catalog.observation(id)!!, notedAt = at)

    private fun hour(offset: Long) = noon.plusHours(offset).toString()

    @Test
    fun `свежее наблюдение работает целиком`() {
        // Птицы бьют над водой: +0.35 хищнику на два часа.
        val factor = observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, hour(0))

        assertNotNull(factor)
        assertEquals(1.35, factor!!.value, 0.001)
        assertTrue(factor.limiting)
        assertEquals(0.0, factor.weight, 0.001)
    }

    @Test
    fun `к середине срока поправка вдвое слабее`() {
        val factor = observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, hour(1))

        // Два часа срока, прошёл один: половина от +0.35.
        assertEquals(1.175, factor!!.value, 0.001)
        assertTrue("должно быть сказано, что осталось: ${factor.comment}", factor.comment.contains("ч"))
    }

    @Test
    fun `после срока наблюдения нет вовсе`() {
        // Не ноль и не единица, а отсутствие фактора: лишней строки на экране
        // быть не должно.
        assertNull(observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, hour(2)))
        assertNull(observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, hour(5)))
    }

    @Test
    fun `часы до отметки наблюдение не касается`() {
        // Оно рассказывает о том, что уже произошло, а не о том, что будет.
        assertNull(observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, hour(-1)))
    }

    @Test
    fun `наблюдение о хищнике не касается мирной рыбы`() {
        val predator = observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, hour(0))
        val peaceful = observationFactor(listOf(noted("birds_diving")), Guild.PEACEFUL, hour(0))

        assertNotNull(predator)
        assertNull(peaceful)
    }

    @Test
    fun `общее наблюдение касается обеих гильдий`() {
        // Радужная плёнка — про гниение: там не берёт никто.
        val predator = observationFactor(listOf(noted("rainbow_film")), Guild.PREDATOR, hour(0))
        val peaceful = observationFactor(listOf(noted("rainbow_film")), Guild.PEACEFUL, hour(0))

        assertEquals(0.6, predator!!.value, 0.001)
        assertEquals(0.6, peaceful!!.value, 0.001)
    }

    @Test
    fun `несколько наблюдений складываются`() {
        val factor = observationFactor(
            listOf(noted("birds_diving"), noted("bait_fish_panic")),
            Guild.PREDATOR,
            hour(0)
        )

        // +0.35 и +0.3 разом.
        assertEquals(1.65.coerceAtMost(1.6), factor!!.value, 0.001)
        assertTrue(factor.comment.contains(";"))
    }

    @Test
    fun `поправка зажата и не заменяет собой погоду`() {
        val many = List(5) { noted("birds_diving") }
        val awful = List(5) { noted("rainbow_film") }

        assertEquals(1.6, observationFactor(many, Guild.PREDATOR, hour(0))!!.value, 0.001)
        assertEquals(0.4, observationFactor(awful, Guild.PREDATOR, hour(0))!!.value, 0.001)
    }

    @Test
    fun `наблюдение с нулевым сроком не применяется`() {
        // Чужой словарь вправе прислать что угодно; делить на ноль не будем.
        val broken = ActiveObservation(
            type = ObservationType(id = "broken", name = "Странное", effect = 0.5, hours = 0),
            notedAt = noon
        )

        assertNull(observationFactor(listOf(broken), Guild.PREDATOR, hour(0)))
    }

    @Test
    fun `непонятное время часа не роняет расчёт`() {
        assertNull(observationFactor(listOf(noted("birds_diving")), Guild.PREDATOR, "не время"))
    }
}
