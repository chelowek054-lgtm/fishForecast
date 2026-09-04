package com.example.fishforecast.domain.water

import com.example.fishforecast.domain.knowledge.WaterBodyType
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

/**
 * Сколько кислорода в воде на самом деле, мг/л.
 *
 * Насыщение — это потолок. Дотягивается ли вода до него, решают течение и
 * ветер: проточная вода аэрируется сама, а стоячий пруд в штиль — нет.
 *
 * Отдельно вычитается ночной провал. Днём растения отдают кислород, ночью
 * дышат наравне со всеми, и к рассвету его меньше всего за сутки. В малом
 * заросшем пруду это решает исход утренней рыбалки, на реке незаметно.
 *
 * @param darkHours сколько часов подряд перед этим не было солнца.
 */
fun availableOxygenMgL(
    waterTemperatureC: Double,
    waterBody: WaterBodyType? = null,
    darkHours: Int = 0,
    windMs: Double = 0.0
): Double {
    val saturation = oxygenSaturationMgL(waterTemperatureC)

    val base = waterBody?.aeration ?: 1.0
    // Рябь и волнение подгоняют воду к насыщению тем сильнее, чем длиннее
    // разгон ветра: на большом озере это заметно, в пруду почти нет.
    val fromWind = (windMs / WIND_FULL_AERATION_MS).coerceIn(0.0, 1.0) *
        WIND_AERATION_GAIN * (waterBody?.windMixing ?: 1.0)
    val aeration = (base + fromWind).coerceIn(0.0, 1.0)

    val sag = (waterBody?.nightOxygenDropMgL ?: 0.0) *
        (darkHours.toDouble() / NIGHT_LENGTH_HOURS).coerceIn(0.0, 1.0)

    return (saturation * aeration - sag).coerceAtLeast(0.0)
}

/** Ветер, при котором перемешивание уже даёт всё, что может. */
private const val WIND_FULL_AERATION_MS = 8.0

/** Насколько ветер способен поднять аэрацию стоячей воды. */
private const val WIND_AERATION_GAIN = 0.15

/** За столько тёмных часов ночной провал набирает полную силу. */
private const val NIGHT_LENGTH_HOURS = 8.0

/**
 * Кислород в яме, мг/л.
 *
 * Пока столб перемешан, яма дышит вместе с мелью: ветер достаёт до дна, и
 * разница только в температуре. Но стоит воде разделиться — а это видно по
 * разнице слоёв, — как термоклин перестаёт пропускать кислород вниз, и яма
 * начинает его тратить: донные отложения потребляют, а взять неоткуда.
 *
 * Отсюда и рыба в термоклине: наверху жарко, внизу нечем дышать, и она
 * стоит между. Раньше расчёт брал для ямы кислород мели и завышал её шансы
 * ровно там, где рыбы нет.
 *
 * @param stratifiedHours сколько часов подряд слои уже разделены.
 */
fun deepOxygenMgL(
    deepTemperatureC: Double,
    shallowTemperatureC: Double,
    waterBody: WaterBodyType? = null,
    stratifiedHours: Int = 0,
    darkHours: Int = 0,
    windMs: Double = 0.0
): Double {
    val stratified = isStratified(shallowTemperatureC, deepTemperatureC)
    if (!stratified) {
        // Перемешанный столб: та же вода, только холоднее.
        return availableOxygenMgL(deepTemperatureC, waterBody, darkHours, windMs)
    }

    // Ветер до ямы не достаёт, дневного кислорода от водорослей там тоже нет:
    // остаётся насыщение по температуре, поправленное аэрацией водоёма.
    val base = oxygenSaturationMgL(deepTemperatureC) * (waterBody?.aeration ?: 1.0)
    val perDay = waterBody?.hypolimnionDropMgLPerDay ?: 0.0
    val spent = perDay * stratifiedHours / HOURS_IN_DAY
    return (base - spent).coerceAtLeast(0.0)
}

/**
 * Разделился ли столб. Судим по разнице слоёв: три градуса между мелью и
 * ямой — это уже термоклин, а не разброс измерений.
 */
fun isStratified(shallowTemperatureC: Double, deepTemperatureC: Double): Boolean =
    shallowTemperatureC - deepTemperatureC >= STRATIFICATION_GRADIENT_C

/** Разница слоёв, начиная с которой считаем воду расслоившейся, °C. */
const val STRATIFICATION_GRADIENT_C = 3.0

private const val HOURS_IN_DAY = 24.0
