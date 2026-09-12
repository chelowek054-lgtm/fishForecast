package com.example.fishforecast.domain.bite

import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.domain.fish.Guild
import com.example.fishforecast.domain.fish.decodeBaits
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.domain.knowledge.StructureType
import kotlin.math.abs

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
     *
     * Структуры не складываются. Точка — одно место, и коряжник с бровкой
     * говорят об одном: рыбе здесь есть где стоять. Сумма уводила балл под
     * потолок от числа галочек, поэтому в расчёт идут самая сильная
     * структура за рыбу и самая сильная против.
     */
    fun bonusFor(guild: Guild): Double {
        if (structures.isEmpty()) return 1.0

        val bonuses = unique.map { structure ->
            when (guild) {
                Guild.PREDATOR -> structure.predatorBonus
                Guild.PEACEFUL -> structure.peacefulBonus
            }
        }
        return (1.0 + strongest(bonuses)).coerceIn(MIN_BONUS, MAX_BONUS)
    }

    /**
     * Поправка к температуре воды: донный ключ холодит своё место. Берётся
     * самая сильная, а не сумма: два родника не холодят на шесть градусов.
     */
    val waterOffsetC: Double
        get() = unique.map { it.waterOffsetC }.maxByOrNull { abs(it) } ?: 0.0

    /** Поправка к кислороду: лучшая прибавка и худшая убыль, без сложения. */
    val oxygenOffsetMgL: Double get() = strongest(unique.map { it.oxygenBonusMgL })

    /** Повтор одной структуры в точке — та же структура, а не вторая. */
    private val unique: List<StructureType> get() = structures.distinctBy { it.id }

    private companion object {
        /** Даже самое гиблое место не обнуляет шанс полностью. */
        const val MIN_BONUS = 0.7

        /** И самое рыбное не заменяет собой погоду: сдвиг не больше трети. */
        const val MAX_BONUS = 1.3
    }
}

/** Лучший довод «за» плюс худший «против»: доводы одной стороны не копятся. */
private fun strongest(values: List<Double>): Double =
    (values.maxOrNull() ?: 0.0).coerceAtLeast(0.0) +
        (values.minOrNull() ?: 0.0).coerceAtMost(0.0)

/** Какой слой воды имеется в виду. */
enum class WaterLayerChoice(val title: String) {
    SHALLOW("на мели"),
    DEEP("в яме")
}

/**
 * Место по сохранённой точке.
 *
 * Структуры лежали у точки с тех пор, как она научилась их хранить, но до
 * расчёта не доходили: экраны передавали один слой, и хищник считался в
 * пустой воде — коряжник, бровка и приток на оценку не влияли.
 *
 * Незнакомые идентификаторы молча пропускаются: словарь правится отдельно от
 * точек, и чужая структура не повод остаться без оценки места.
 */
fun placeOf(
    spot: FishingSpotEntity?,
    layer: WaterLayerChoice,
    catalog: KnowledgeCatalog
): PlaceContext {
    val structures = spot?.structures?.decodeBaits()?.mapNotNull { catalog.structure(it) }.orEmpty()
    return PlaceContext(
        layer = layer,
        structures = structures,
        // Имя точки важнее слова «мель»: рыболов узнаёт своё место по имени,
        // а слой он и так выбрал сам.
        title = spot?.name?.takeIf { it.isNotBlank() } ?: layer.title
    )
}
