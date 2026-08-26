package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запись из журнала трофеев.
 *
 * Погода и оценка клёва сохраняются снимком на момент поимки: прогноз в
 * кэше живёт недолго, а сверять предсказание с реальным уловом можно будет
 * только по тому, что было в тот час. Ссылки на рыбу и точку обнуляются
 * при их удалении — улов остаётся фактом, даже если справочник изменили.
 */
@Entity(
    tableName = "catches",
    foreignKeys = [
        ForeignKey(
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FishingSpotEntity::class,
            parentColumns = ["id"],
            childColumns = ["spotId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("fishId"), Index("spotId")]
)
data class CatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fishId: Int? = null,
    val spotId: Int? = null,
    /** Файл во внутренней памяти приложения; null, если снимок не делали. */
    val photoPath: String? = null,
    val weightGrams: Int? = null,
    val lengthCm: Int? = null,
    val note: String = "",
    val caughtAt: Long = System.currentTimeMillis(),

    // Снимок условий на момент поимки.
    val temperature: Double? = null,
    val pressureMmHg: Double? = null,
    val windSpeed: Double? = null,
    /** Что показывал Bite Score в этот час — для последующей сверки. */
    val biteScore: Int? = null
)
