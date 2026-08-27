package com.example.fishforecast.ui.reference

import com.example.fishforecast.domain.fish.GroundbaitRule

/**
 * Перевод словарных значений справочника на человеческий.
 *
 * В самом справочнике они остаются кодами (`low`, `sweet_fruity`): он ходит
 * между устройствами и сервером, и перевод — дело экрана, а не документа.
 * Незнакомое значение возвращается как есть: чужой справочник может знать
 * слова, которых это приложение ещё не выучило.
 */
fun volumeText(value: String): String = when (value) {
    "none" -> "не кормить"
    "low" -> "мало корма"
    "medium" -> "умеренно"
    "high" -> "обильно"
    else -> value
}

fun fractionText(value: String): String = when (value) {
    "none" -> "—"
    "fine" -> "мелкая фракция"
    "medium" -> "средняя фракция"
    "coarse" -> "крупная фракция"
    else -> value
}

fun sweetnessText(value: String): String = when (value) {
    "none" -> "без сладости"
    "low" -> "чуть сладкая"
    "medium" -> "умеренно сладкая"
    "high" -> "сладкая"
    else -> value
}

fun flavorText(value: String): String = when (value) {
    "none" -> "без аромата"
    "spicy_salty" -> "пряно-солёный аромат"
    "sweet_fruity" -> "сладко-фруктовый аромат"
    "garlic_hemp" -> "чеснок и конопля"
    "vanilla_honey" -> "ваниль и мёд"
    "fish_blood" -> "рыба и кровь"
    "meat_spicy" -> "мясной пряный аромат"
    "sweet_spicy" -> "сладко-пряный аромат"
    else -> value.replace('_', ' ')
}

fun horizonText(value: String): String = when (value) {
    "bottom" -> "у дна"
    "mid" -> "в толще"
    "top" -> "у поверхности"
    else -> value
}

/** Строка правила прикормки без пустых мест. */
fun GroundbaitRule.summary(): String = listOf(
    volumeText(volume),
    fractionText(fraction),
    sweetnessText(sweetness),
    flavorText(flavorProfile)
).filter { it != "—" }.joinToString(", ")
