package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Отметка о том, что рыболов увидел своими глазами.
 *
 * Прогноз говорит, какой должна быть вода; наблюдение говорит, какая она есть.
 * Бой малька у камыша или радужная плёнка на воде — это факт, и он весомее
 * расчёта, но живёт недолго: через два часа малёк уже в другом углу.
 *
 * Поэтому у отметки нет поля «действует до»: срок хранится в словаре знаний
 * рядом с самим наблюдением и может измениться вместе с ним. Здесь только
 * время, когда рыболов это увидел.
 */
@Entity(tableName = "observations", indices = [Index("mapId")])
data class ObservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uid: String = UUID.randomUUID().toString(),
    /** Район, на котором это видели: чужой водоём отметка не касается. */
    val mapId: Int,
    /** Идентификатор из словаря наблюдений: `fish_rolling`, `birds_diving`. */
    val typeId: String,
    val notedAt: Long = System.currentTimeMillis()
)
