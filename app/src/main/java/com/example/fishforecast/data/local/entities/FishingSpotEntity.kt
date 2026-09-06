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
 * Секретная точка.
 *
 * [mapId] говорит, какому району точка принадлежит. Раньше принадлежности не
 * было вовсе: точку относили к району по геометрии — попадает в границы,
 * значит его. Из-за этого точка удалённого района оставалась в базе навсегда,
 * невидимая и никому не нужная, а точка в границах двух районов принадлежала
 * обоим сразу.
 *
 * Удаление района уносит его точки: место без района — это координаты без
 * воды, погоды и расчёта. Поэтому CASCADE, а не SET_NULL.
 *
 * [fishId] связывает точку со справочником: по нему видно, какая рыба здесь
 * берёт. Здесь наоборот SET_NULL — удаление вида из справочника не должно
 * уносить место, привязка просто обнуляется.
 */
@Entity(
    tableName = "fishing_spots",
    foreignKeys = [
        ForeignKey(
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SavedMapEntity::class,
            parentColumns = ["id"],
            childColumns = ["mapId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fishId"), Index("mapId")]
)
data class FishingSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** Глобальный идентификатор точки — ключ при обмене и в общей базе. */
    val uid: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    /**
     * Район, которому принадлежит точка.
     *
     * Пусто — точка досталась от времён, когда принадлежности не было, и ни в
     * один сохранённый район не попадает по координатам. Такую точку видно в
     * списке «ничьих», пока рыболов не заведёт нужный район.
     */
    val mapId: Int? = null,
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
