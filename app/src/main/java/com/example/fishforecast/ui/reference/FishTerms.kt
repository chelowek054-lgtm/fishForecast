package com.example.fishforecast.ui.reference

import com.example.fishforecast.domain.fish.GroundbaitRule
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.fish.flavorText
import com.example.fishforecast.domain.fish.horizonText

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

/** Гильдия словами: рыболов думает «хищник», а не `predator`. */
fun guildText(guild: Guild): String = when (guild) {
    Guild.PREDATOR -> "хищник"
    Guild.PEACEFUL -> "мирная"
}


/** Строка правила прикормки без пустых мест. */
fun GroundbaitRule.summary(): String = listOf(
    volumeText(volume),
    fractionText(fraction),
    sweetnessText(sweetness),
    flavorText(flavorProfile)
).filter { it != "—" }.joinToString(", ")
