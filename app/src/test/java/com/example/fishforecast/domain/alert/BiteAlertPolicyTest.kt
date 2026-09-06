package com.example.fishforecast.domain.alert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

/**
 * Политика уведомлений.
 *
 * Уведомление — единственное, что приложение делает без спроса. Поэтому здесь
 * проверяется не то, что оно приходит, а то, что оно **не** приходит, когда
 * приходить не должно: ночью, дважды об одном, и главное — когда доехать всё
 * равно не успеть.
 */
class BiteAlertPolicyTest {

    /** Полдень: рабочее время политики, чтобы тихие часы не мешали проверке. */
    private val noon = LocalDateTime.parse("2026-09-10T12:00")

    private fun decide(
        windowIn: Duration = Duration.ofHours(6),
        windowScore: Int = 85,
        currentScore: Int? = 50,
        now: LocalDateTime = noon,
        travel: Duration? = Duration.ofMinutes(40),
        history: AlertHistory = AlertHistory()
    ) = decideAlert(
        windowTime = now.plus(windowIn),
        windowScore = windowScore,
        currentScore = currentScore,
        now = now,
        travel = travel,
        history = history
    )

    private fun reasonOf(decision: AlertDecision): String =
        (decision as AlertDecision.Skip).reason

    // ---------- ради чего всё затевалось ----------

    @Test
    fun `не зовёт, когда до воды дольше, чем до окна`() {
        // Клёв через час, а ехать три: звать — значит только расстроить.
        val decision = decide(windowIn = Duration.ofHours(1), travel = Duration.ofHours(3))

        assertTrue(reasonOf(decision).contains("не успеть"))
    }

    @Test
    fun `зовёт, когда дороги и сборов хватает с запасом`() {
        val decision = decide(windowIn = Duration.ofHours(6), travel = Duration.ofHours(2))

        assertTrue("должен позвать: $decision", decision is AlertDecision.Notify)
    }

    @Test
    fun `на грани не зовёт, потому что нужны ещё и сборы`() {
        // Ехать час, до окна час пятнадцать: снасти собрать уже некогда.
        val decision = decide(
            windowIn = Duration.ofMinutes(75),
            travel = Duration.ofHours(1)
        )

        assertTrue(reasonOf(decision).contains("не успеть"))
    }

    @Test
    fun `без геолокации запас берётся осторожный`() {
        // Место неизвестно, до окна полтора часа — молчим: вдруг ехать далеко.
        val near = decide(windowIn = Duration.ofMinutes(90), travel = null)
        assertTrue(reasonOf(near).contains("не успеть"))

        // А за пять часов доедешь почти куда угодно.
        val far = decide(windowIn = Duration.ofHours(5), travel = null)
        assertTrue("$far", far is AlertDecision.Notify)
    }

    // ---------- не быть назойливым ----------

    @Test
    fun `ночью молчит`() {
        val night = LocalDateTime.parse("2026-09-10T03:00")
        val decision = decide(now = night)

        assertTrue(reasonOf(decision).contains("тихие часы"))
    }

    @Test
    fun `поздним вечером тоже молчит`() {
        val decision = decide(now = LocalDateTime.parse("2026-09-10T22:30"))

        assertTrue(reasonOf(decision).contains("тихие часы"))
    }

    @Test
    fun `об одном окне зовёт один раз`() {
        val windowTime = noon.plusHours(6)
        val decision = decide(
            history = AlertHistory(lastWindowTime = windowTime.toString())
        )

        assertEquals("об этом окне уже звали", reasonOf(decision))
    }

    @Test
    fun `не зовёт чаще раза в двенадцать часов`() {
        val decision = decide(
            history = AlertHistory(lastNotifiedAt = noon.minusHours(4))
        )

        assertTrue(reasonOf(decision).contains("прошлого раза"))
    }

    @Test
    fun `после кулдауна зовёт снова`() {
        val decision = decide(
            history = AlertHistory(lastNotifiedAt = noon.minusHours(13))
        )

        assertTrue("$decision", decision is AlertDecision.Notify)
    }

    // ---------- звать только с новостью ----------

    @Test
    fun `не зовёт, если и сейчас клюёт не хуже`() {
        val decision = decide(windowScore = 80, currentScore = 75)

        assertTrue(reasonOf(decision).contains("не та разница"))
    }

    @Test
    fun `зовёт, когда разница заметная`() {
        val decision = decide(windowScore = 85, currentScore = 40)

        assertTrue("$decision", decision is AlertDecision.Notify)
    }

    @Test
    fun `за сутки вперёд звать рано`() {
        val decision = decide(windowIn = Duration.ofHours(20))

        assertTrue(reasonOf(decision).contains("рано звать"))
    }

    @Test
    fun `о прошедшем окне не зовёт`() {
        val decision = decide(windowIn = Duration.ofHours(-1))

        assertEquals("окно уже началось", reasonOf(decision))
    }

    // ---------- дорога ----------

    @Test
    fun `время в пути растёт вместе с расстоянием`() {
        // Москва — Тверь, около 160 км по прямой.
        val near = travelTime(55.75, 37.62, 55.75, 38.20)
        val far = travelTime(55.75, 37.62, 56.86, 35.90)

        assertTrue("ближнее $near должно быть меньше дальнего $far", near < far)
        assertTrue("до Твери не пятнадцать минут: $far", far > Duration.ofHours(2))
    }

    @Test
    fun `до своего пруда дорога короткая`() {
        // Десять километров: с учётом извилистости — минут пятнадцать.
        val travel = travelTime(55.75, 37.62, 55.84, 37.62)

        assertTrue("$travel", travel < Duration.ofMinutes(30))
    }
}
