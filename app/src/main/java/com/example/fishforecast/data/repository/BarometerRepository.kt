package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.PressureLogDao
import com.example.fishforecast.data.local.entities.PressureLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * История собственного барометра.
 *
 * Датчик молчит, пока приложение закрыто, поэтому ряд получается рваным —
 * и это нормально: он не заменяет прогноз, а проверяет его в тех часах,
 * когда рыболов был с телефоном.
 */
@Singleton
class BarometerRepository @Inject constructor(
    private val dao: PressureLogDao
) {
    val log: Flow<List<PressureLogEntity>> = dao.getLog()

    /** Пишет не чаще раза в час: за час давление меняется на десятые. */
    suspend fun record(pressureHpa: Float, at: LocalDateTime = LocalDateTime.now()) {
        dao.insert(
            PressureLogEntity(
                time = at.truncatedTo(ChronoUnit.HOURS).format(HOUR_FORMAT),
                pressure = pressureHpa.toDouble()
            )
        )
        dao.deleteOlderThan(
            at.minusDays(HISTORY_DAYS).truncatedTo(ChronoUnit.HOURS).format(HOUR_FORMAT)
        )
    }

    private companion object {
        val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        /** Дальше история не нужна: клёв решают часы, а не месяцы. */
        const val HISTORY_DAYS = 7L
    }
}
