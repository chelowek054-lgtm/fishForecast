package com.example.fishforecast.domain.bite

/** Насколько активна рыба в конкретный час. */
data class BiteForecast(
    /** Время из прогноза, ISO8601 — тот же ключ, что у WeatherEntity. */
    val time: String,
    /** Итоговая оценка, 0..100. */
    val score: Int,
    val level: BiteLevel,
    /** Что именно повлияло — чтобы рыболов видел причину, а не одно число. */
    val factors: List<BiteFactor>
)

enum class BiteLevel {
    POOR,
    MODERATE,
    GOOD;

    companion object {
        fun fromScore(score: Int): BiteLevel = when {
            score >= 70 -> GOOD
            score >= 40 -> MODERATE
            else -> POOR
        }
    }
}

/**
 * Отдельный фактор оценки, [value] — 0..1.
 *
 * [limiting] отличает ограничители среды (температура, кислород) от условий
 * клёва (давление, тенденция, ветер). Ограничитель нельзя компенсировать
 * ничем другим: если рыба не может кормиться в такой воде, удачное давление
 * ей не поможет — поэтому такие факторы умножаются, а не складываются, и
 * [weight] для них не используется.
 */
data class BiteFactor(
    val name: String,
    val value: Double,
    val weight: Double,
    val comment: String,
    val limiting: Boolean = false
)
