package com.example.fishforecast.domain.bite

import com.example.fishforecast.domain.sensor.hPaToMmHg
import kotlin.math.pow

/**
 * Норма давления — свойство места, а не знание рыболова.
 *
 * Раньше её вводили руками, и это требовало от новичка того, чего у него
 * нет: цифру можно было взять только со своего барометра. Привычный фон
 * водоёма — это многолетнее среднее давление на его высоте, и приложение
 * считает его само: по наблюдениям, а без сети — по высоте места.
 */

/**
 * Среднее давление по ряду наблюдений, мм рт. ст.
 *
 * Пропуски в ряду — обычное дело: модель прогноза хранит ограниченную
 * историю, и дальний её край приходит пустым. Но короткий ряд усредняет не
 * норму, а погоду текущей недели, поэтому меньше [MIN_SAMPLE_HOURS] часов
 * считается недостаточным.
 */
fun averagePressureMmHg(pressuresHpa: List<Double?>): Double? {
    val samples = pressuresHpa.filterNotNull()
    if (samples.size < MIN_SAMPLE_HOURS) return null
    return samples.average().hPaToMmHg()
}

/**
 * Давление стандартной атмосферы на заданной высоте, мм рт. ст.
 *
 * Запасной вариант, когда истории нет: воздух над водоёмом в среднем ведёт
 * себя как стандартная атмосфера, и одна только высота даёт цифру с
 * точностью до пары миллиметров. Под Москвой (152 м) формула даёт 746, а
 * среднее за семьдесят суток наблюдений — 746: разница в пределах ошибки
 * округления.
 */
fun standardPressureMmHg(elevationM: Double): Double {
    val hPa = SEA_LEVEL_HPA * (1 - TEMPERATURE_LAPSE * elevationM).pow(BAROMETRIC_EXPONENT)
    return hPa.hPaToMmHg()
}

/** Часов наблюдений, ниже которых среднее описывает погоду, а не норму. */
const val MIN_SAMPLE_HOURS = 14 * 24

private const val SEA_LEVEL_HPA = 1013.25
private const val TEMPERATURE_LAPSE = 2.25577e-5
private const val BAROMETRIC_EXPONENT = 5.25588
