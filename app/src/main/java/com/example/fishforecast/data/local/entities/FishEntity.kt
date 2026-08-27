package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Вид рыбы из справочника.
 *
 * Раньше здесь лежал один диапазон температуры «от и до». Но рыба не
 * выключается на границе: есть оптимум, в котором она кормится, и есть
 * предел выносливости, за которым ей уже не до еды. Между ними активность
 * тает постепенно, и разница у видов огромна — налим при 12 °C в оптимуме,
 * карп в этой воде вялый.
 *
 * Кислород, горизонт и стол (наживки с прикормкой) тоже свойства вида, а не
 * общие константы: в одной и той же прогретой воде амуру привольно, а карпу
 * нечем дышать, и кормить их надо по-разному.
 */
@Entity(tableName = "fish")
data class FishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * Глобальный идентификатор вида: в справочнике это осмысленный слаг
     * (`carp`, `pike`), а у видов, заведённых рыболовом вручную, — случайный.
     * По нему справочник обновляется с сервера и переживает обмен пакетами.
     */
    val uid: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",

    /** Оптимум: в этих градусах рыба кормится охотно. */
    val optMinTemp: Float,
    val optMaxTemp: Float,
    /** Предел выносливости: за ним активность уходит в ноль. */
    val absMinTemp: Float,
    val absMaxTemp: Float,

    val minPressure: Float,
    val maxPressure: Float,

    /** Кислорода вдоволь, мг/л: ниже — рыба начинает беречь силы. */
    val oxygenComfortMgL: Float = 5f,
    /** Кислорода критически мало, мг/л: рыбе не до еды. */
    val oxygenCriticalMgL: Float = 3f,

    /** Где держится: `bottom`, `mid`, `top`. */
    val defaultHorizon: String = HORIZON_BOTTOM,
    /**
     * Температура воды, ниже которой у вида «холодный» стол: животные
     * наживки вместо растительных, меньше корма, другие ароматы.
     */
    val coldTempThreshold: Float = 12f,

    /** Наживки в холодной и тёплой воде, JSON-массивы строк. */
    val baitsCold: String = EMPTY_LIST,
    val baitsWarm: String = EMPTY_LIST,
    /** Правила прикормки, JSON: объём, фракция, сладость, аромат, заметка. */
    val groundbaitCold: String = EMPTY_OBJECT,
    val groundbaitWarm: String = EMPTY_OBJECT,

    val imageUrl: String? = null
) {
    companion object {
        const val HORIZON_BOTTOM = "bottom"
        const val EMPTY_LIST = "[]"
        const val EMPTY_OBJECT = "{}"
    }
}
