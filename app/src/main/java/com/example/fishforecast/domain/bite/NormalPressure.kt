package com.example.fishforecast.domain.bite

/**
 * Какую норму давления брать в расчёт.
 *
 * Норма — свойство водоёма, а не рыбы: общей цифры не существует, рыба
 * привыкает к своему фону. Карта задаёт норму всего района, но внутри
 * одной области могут оказаться разные водоёмы, поэтому точка вправе
 * уточнить значение для себя.
 */
fun resolveNormalPressure(
    mapNormalMmHg: Double?,
    spotNormalMmHg: Double?
): Double? = spotNormalMmHg ?: mapNormalMmHg
