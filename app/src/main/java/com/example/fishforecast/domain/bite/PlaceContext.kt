package com.example.fishforecast.domain.bite

import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.knowledge.StructureType

/**
 * Место, для которого считается клёв.
 *
 * До сих пор оценка была одна на весь водоём и считалась по мели. Но
 * рыболову нужен ответ не «ехать ли», а «куда встать»: в одну и ту же
 * погоду мель кипит, а яма стоит, и наоборот. Слой и структуры — это и
 * есть место.
 */
data class PlaceContext(
    val layer: WaterLayerChoice = WaterLayerChoice.SHALLOW,
    /** Структуры этого места из словаря знаний. */
    val structures: List<StructureType> = emptyList(),
    val title: String = layer.title
) {
    /**
     * Насколько структуры места помогают этому виду.
     *
     * Множитель, а не слагаемое: коряжник не заменяет кислород и не отменяет
     * давление — он поднимает или роняет шанс на том, что осталось. Место
     * без особенностей даёт ровно единицу и ничего не меняет.
     */
    fun bonusFor(guild: Guild): Double {
        if (structures.isEmpty()) return 1.0

        val sum = structures.sumOf { structure ->
            when (guild) {
                Guild.PREDATOR -> structure.predatorBonus
                Guild.PEACEFUL -> structure.peacefulBonus
            }
        }
        return (1.0 + sum).coerceIn(MIN_BONUS, MAX_BONUS)
    }

    /** Поправка к температуре воды: донный ключ холодит своё место. */
    val waterOffsetC: Double get() = structures.sumOf { it.waterOffsetC }

    /** Поправка к кислороду: приток добавляет, гнилой ил отнимает. */
    val oxygenOffsetMgL: Double get() = structures.sumOf { it.oxygenBonusMgL }

    private companion object {
        /** Даже самое гиблое место не обнуляет шанс полностью. */
        const val MIN_BONUS = 0.4

        /** И самое рыбное не заменяет собой погоду. */
        const val MAX_BONUS = 1.6
    }
}

/** Какой слой воды имеется в виду. */
enum class WaterLayerChoice(val title: String) {
    SHALLOW("на мели"),
    DEEP("в яме")
}
