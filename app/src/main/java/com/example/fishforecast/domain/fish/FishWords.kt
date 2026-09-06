package com.example.fishforecast.domain.fish

/**
 * Словарь справочника по-русски.
 *
 * Живёт в домене, а не на экране, потому что теми же словами говорит план на
 * выезд: одно и то же значение не должно называться на двух экранах по-разному.
 *
 * Ключи приходят из справочника и могут быть незнакомыми — чужой словарь вправе
 * знать больше, чем эта сборка. Тогда возвращается сам ключ: показать
 * непонятное честнее, чем подставить похожее.
 */

/**
 * Где вид держится.
 *
 * Значения те же, что в `initial_fish.json`: `bottom`, `midwater`, `surface`.
 * Раньше здесь ждали `mid` и `top`, которых в справочнике нет, — и плотва со
 * щукой получали на экране английское «midwater», а амур «surface».
 */
fun horizonText(value: String): String = when (value) {
    "bottom" -> "у дна"
    "midwater", "mid" -> "в толще"
    "surface", "top" -> "у поверхности"
    else -> value
}

/** Тот же горизонт, но как строка совета: «Дно», «Полводы», «Верх». */
fun horizonTitle(value: String): String = when (value) {
    "bottom" -> "Дно"
    "midwater", "mid" -> "Полводы"
    "surface", "top" -> "Верх"
    else -> value
}

/**
 * Чем пахнет стол.
 *
 * Перечислены все профили, которые встречаются в справочнике. До этого их было
 * восемь из пятнадцати, и лещу с плотвой на экран выходило «coriander spicy» и
 * «chocolate anis» — ключ вместо слова.
 */
fun flavorText(value: String): String = when (value) {
    "none" -> "без аромата"
    "spicy_salty" -> "пряно-солёный"
    "sweet_fruity" -> "сладко-фруктовый"
    "garlic_hemp" -> "чеснок и конопля"
    "vanilla_honey" -> "ваниль и мёд"
    "coriander_spicy" -> "кориандр и корица"
    "caramel_vanilla" -> "карамель и ваниль"
    "anis_hemp" -> "анис и конопля"
    "chocolate_anis" -> "шоколад и анис"
    "fish_blood" -> "рыба и кровь"
    "blood_fishmeal" -> "кровь и рыбная мука"
    "fishmeal" -> "рыбная мука"
    "liver_smell" -> "печень и потроха"
    "herbal" -> "травяной"
    "grass_sweetcorn" -> "трава и кукуруза"
    "meat_spicy" -> "мясной пряный"
    "sweet_spicy" -> "сладко-пряный"
    else -> value.replace('_', ' ')
}
