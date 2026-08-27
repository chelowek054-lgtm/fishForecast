package com.example.fishforecast.data.local.entities

import androidx.room.Entity

/**
 * Показания барометра устройства по часам.
 *
 * Своя история давления точнее сетевого прогноза и не требует связи: она
 * снята там, где рыболов стоял. Именно по ней видно, что давление уже
 * пошло к норме, — прогноз узнает об этом позже.
 *
 * Ключ — час: чаще писать незачем, за час давление меняется на десятые.
 */
@Entity(tableName = "barometer_log", primaryKeys = ["time"])
data class PressureLogEntity(
    /** ISO8601 до часа, местное время. */
    val time: String,
    /** Станционное давление, гПа — как отдаёт датчик. */
    val pressure: Double
)
