package com.example.fishforecast.domain.weather

import kotlin.math.roundToInt

/**
 * Ветер в приложении хранится так, как отдаёт Open-Meteo: скорость в км/ч,
 * направление — откуда дует, в градусах. Рыболову привычнее м/с и румб,
 * поэтому пересчёт живёт здесь, а не в разметке экрана.
 */
private const val KMH_PER_MS = 3.6

fun Double.kmhToMs(): Double = this / KMH_PER_MS

private val COMPASS = listOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")

/** Румб, откуда дует ветер: 0° — северный, 90° — восточный. */
fun windDirectionLabel(degrees: Double): String {
    val normalized = ((degrees % 360) + 360) % 360
    val sector = ((normalized / 45.0).roundToInt()) % COMPASS.size
    return COMPASS[sector]
}

/**
 * Куда смотрит стрелка на экране. Значок стрелки нарисован вверх, то есть
 * «на север», а ветер надо показать летящим ОТ своего румба: северный дует
 * на юг, поэтому к направлению добавляется половина оборота.
 */
fun windArrowRotation(degrees: Double): Float = ((degrees + 180) % 360).toFloat()

/**
 * Насколько развернулся ветер, в градусах: от 0 до 180.
 *
 * Смена направления важнее самой скорости: она означает, что через водоём
 * прошёл фронт, а рыба на такое отзывается паузой — ей нужно заново найти,
 * где теперь стоит корм.
 */
fun windTurn(fromDegrees: Double, toDegrees: Double): Double {
    val delta = ((toDegrees - fromDegrees) % 360 + 360) % 360
    return if (delta > 180) 360 - delta else delta
}

/**
 * Северный ли ветер: сектор от северо-запада до северо-востока.
 *
 * Он приносит холодный воздух, студит верхний слой и в холодной воде
 * работает против рыболова, а в перегретой — на него.
 */
fun isNortherlyWind(degrees: Double): Boolean {
    val normalized = ((degrees % 360) + 360) % 360
    return normalized >= 315.0 || normalized <= 45.0
}

fun windDescription(speedKmh: Double): String {
    val ms = speedKmh.kmhToMs()
    return when {
        ms < 1.5 -> "штиль"
        ms < 4 -> "слабый"
        ms < 8 -> "умеренный"
        ms < 14 -> "сильный"
        else -> "штормовой"
    }
}
