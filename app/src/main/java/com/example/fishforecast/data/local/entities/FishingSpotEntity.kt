package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val fishId: Int? = null,
    val note: String = "",
    /**
     * Норма давления именно этого водоёма, мм рт. ст. Общей цифры не
     * существует: рыба привыкает к своему фону, и важно отклонение от него,
     * а не абсолютное значение. null — норма не выяснена.
     */
    val normalPressureMmHg: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
