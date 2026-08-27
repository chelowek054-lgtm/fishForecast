package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сохранённая карта — район рыбалки и главный контекст приложения.
 *
 * От выбранной карты зависит всё остальное: по её центру запрашивается
 * погода, её норма давления идёт в расчёт клёва, её границы отбирают точки.
 * Сами тайлы лежат в базе MapLibre, здесь — описание, по которому карту
 * показывают в списке и находят в OfflineManager.
 */
@Entity(tableName = "saved_maps")
data class SavedMapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    /** Идентификатор региона в OfflineManager MapLibre. */
    val offlineRegionId: Long,
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
    val minZoom: Double,
    val maxZoom: Double,
    val sizeBytes: Long = 0,
    /**
     * Норма давления водоёма, мм рт. ст. Общей цифры не существует: рыба
     * привыкает к своему фону, и важно отклонение от него. Точка внутри
     * карты может уточнить это значение для себя.
     */
    val normalPressureMmHg: Double? = null,
    /**
     * Глубины района, м: мель и яма. Батиметрии пруда нет ни в одном
     * открытом источнике, но рыболов свой водоём знает, а без глубины
     * температуру воды не посчитать: она и задаёт инерцию слоя.
     */
    val shallowDepthM: Double? = null,
    val deepDepthM: Double? = null,
    /** Замер воды термометром, °C: факт всегда главнее расчёта. */
    val waterTempC: Double? = null,
    /** Когда сделан замер, ISO8601 без секунд; null — замера не было. */
    val waterTempAt: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Погода запрашивается по центру района. */
    val centerLatitude: Double get() = (north + south) / 2
    val centerLongitude: Double get() = (east + west) / 2

    /** Точка принадлежит карте, если попадает в её границы. */
    fun contains(latitude: Double, longitude: Double): Boolean =
        latitude in south..north && longitude in west..east

    /** Охват района по широте, км — для человекочитаемого описания. */
    val heightKm: Double get() = (north - south) * KM_PER_DEGREE_LATITUDE

    /**
     * Охват по долготе, км. Меридианы сходятся к полюсам, поэтому градус
     * долготы короче градуса широты — тем сильнее, чем дальше от экватора.
     */
    val widthKm: Double
        get() = (east - west) * KM_PER_DEGREE_LATITUDE *
            kotlin.math.cos(Math.toRadians(centerLatitude))

    private companion object {
        const val KM_PER_DEGREE_LATITUDE = 111.32
    }
}
