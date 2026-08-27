package com.example.fishforecast.domain.water

import kotlin.math.exp

/**
 * Кислород в воде.
 *
 * Датчика нет и не будет, но растворимость кислорода — не загадка, а функция
 * температуры: чем теплее вода, тем меньше она способна удержать. Отсюда и
 * связка, на которой держится вся модель клёва: прогрелась — дышать нечем,
 * остыла — рыба ожила.
 *
 * Считается потолок, то есть насыщение. Сколько кислорода в воде на самом
 * деле, зависит ещё от цветения и от того, как её перемешивает ветер, —
 * поэтому ветер остаётся отдельным фактором и не подмешивается сюда.
 */

/**
 * Насыщение кислородом пресной воды, мг/л, по уравнению Бенсона — Краузе.
 *
 * Опорные значения: 14.6 мг/л при 0 °C, 11.3 при 10, 9.1 при 20, 8.2 при 25,
 * 7.5 при 30. Между 20 и 30 градусами вода теряет пятую часть ёмкости — это
 * и есть та самая летняя духота, в которую карп перестаёт брать.
 */
fun oxygenSaturationMgL(waterTemperatureC: Double): Double {
    val kelvin = waterTemperatureC + KELVIN_OFFSET
    val lnO2 = -139.34411 +
        1.575701e5 / kelvin -
        6.642308e7 / kelvin.pow2() +
        1.243800e10 / kelvin.pow3() -
        8.621949e11 / kelvin.pow4()
    return exp(lnO2)
}

/** Как рыболову понимать цифру. */
enum class OxygenLevel { RICH, ENOUGH, LOW, CRITICAL }

/**
 * Пороги приняты по практике прудового хозяйства: 5 мг/л и выше — рыба
 * кормится, ниже 4 — угнетена, ниже 3 — не до еды.
 */
fun oxygenLevel(oxygenMgL: Double): OxygenLevel = when {
    oxygenMgL >= 8.0 -> OxygenLevel.RICH
    oxygenMgL >= 5.0 -> OxygenLevel.ENOUGH
    oxygenMgL >= 3.0 -> OxygenLevel.LOW
    else -> OxygenLevel.CRITICAL
}

fun oxygenLevelText(level: OxygenLevel): String = when (level) {
    OxygenLevel.RICH -> "кислорода много"
    OxygenLevel.ENOUGH -> "кислорода хватает"
    OxygenLevel.LOW -> "кислорода мало"
    OxygenLevel.CRITICAL -> "рыба задыхается"
}

private const val KELVIN_OFFSET = 273.15

private fun Double.pow2() = this * this
private fun Double.pow3() = this * this * this
private fun Double.pow4() = pow2() * pow2()
