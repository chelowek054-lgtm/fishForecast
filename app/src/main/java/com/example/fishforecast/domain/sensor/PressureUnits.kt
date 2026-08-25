package com.example.fishforecast.domain.sensor

/**
 * Open-Meteo и барометр Android работают в гПа, а справочник рыб —
 * в мм рт. ст. Конвертация нужна везде, где эти данные встречаются.
 */
private const val HPA_PER_MMHG = 1.333224f

fun Float.hPaToMmHg(): Float = this / HPA_PER_MMHG

fun Double.hPaToMmHg(): Double = this / HPA_PER_MMHG
