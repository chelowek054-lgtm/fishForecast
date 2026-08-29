package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Где стоит точка. Разговор на воде идёт именно так: «встал на мысу» и
 * «бросал в яму» — это разные точки, и путать их нельзя.
 */
enum class SpotPlacement {
    /** Место на берегу: подход, стоянка, номер сектора. */
    SHORE,

    /** Место в воде: яма, бровка, стол, окно в траве. */
    WATER
}

/**
 * Секретная точка. [fishId] связывает её со справочником: по нему видно,
 * какая рыба здесь берёт, и Bite Score из Фазы 4 сможет считать прогноз
 * прямо для точки. Удаление рыбы из справочника не должно уносить точку —
 * место остаётся, привязка обнуляется.
 */
@Entity(
    tableName = "fishing_spots",
    foreignKeys = [
        ForeignKey(
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("fishId")]
)
data class FishingSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** Глобальный идентификатор точки — ключ при обмене и в общей базе. */
    val uid: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val fishId: Int? = null,
    val note: String = "",
    /**
     * [SpotPlacement] строкой. Точка на берегу и точка в воде — разные
     * вещи: первая говорит, где встать, вторая — куда забрасывать.
     */
    val placement: String = SpotPlacement.WATER.name,
    /**
     * Структуры этого места, JSON-массив идентификаторов из словаря знаний:
     * коряжник, бровка, приток. Ими место и отличается от соседнего.
     */
    val structures: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
