package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Выезд на рыбалку от начала до конца.
 *
 * Приложение умело предсказывать и умело записывать улов, но между ними
 * зияла дыра: чем всё закончилось, если не поймал ничего. А это и есть
 * самые ценные данные — прогноз обещал клёв, поклёвок не было, и понять
 * почему можно только сравнив план с тем, что вышло.
 *
 * Поэтому здесь лежит всё сразу: что рыболов собирался делать, какие были
 * условия на старте, что посоветовало приложение и чем дело кончилось.
 */
@Entity(tableName = "fishing_sessions")
data class FishingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uid: String = UUID.randomUUID().toString(),
    /** Район, на котором ловили. */
    val mapId: Int? = null,

    val startedAt: Long = System.currentTimeMillis(),
    /** null — рыбалка ещё идёт. */
    val finishedAt: Long? = null,

    // Анкета перед выездом.
    val targetFishId: Int? = null,
    val methodId: String? = null,
    /** `SHALLOW` или `DEEP`. */
    val layer: String = "SHALLOW",
    /** Структуры места, JSON-массив идентификаторов. */
    val structures: String = "[]",
    val hasGroundbait: Boolean = true,

    // Снимок условий на старте: прогноз в кэше живёт неделю, а разбирать
    // выезд можно и через год.
    val waterTempC: Double? = null,
    val oxygenMgL: Double? = null,
    val pressureMmHg: Double? = null,
    val windMs: Double? = null,
    val lightPhase: String? = null,
    val biteScore: Int? = null,

    /** Что посоветовало приложение, человекочитаемым текстом. */
    val plan: String = "",

    // Итог.
    val caughtCount: Int? = null,
    val resultNote: String = "",
    /** Как прошло, 1–5: субъективно, но сравнимо между выездами. */
    val rating: Int? = null
) {
    val finished: Boolean get() = finishedAt != null
}
