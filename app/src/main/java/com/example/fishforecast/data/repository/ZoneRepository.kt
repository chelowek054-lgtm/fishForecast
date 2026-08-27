package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.ZoneDao
import com.example.fishforecast.data.local.entities.SectorEntity
import com.example.fishforecast.data.local.entities.ZoneEntity
import com.example.fishforecast.domain.share.GeoPoint
import com.example.fishforecast.domain.share.decodeOutline
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Границы, обведённые рыболовом.
 *
 * Карт контуров и глубин для прудов в открытых источниках нет, зато они
 * есть в голове у того, кто там ловит. Зона — это его знание, записанное
 * так, чтобы пережить обмен пакетами и попасть в общую базу.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ZoneRepository @Inject constructor(
    private val dao: ZoneDao,
    private val fishingContext: FishingContextRepository
) {
    /** Зоны выбранного района. */
    val activeZones: Flow<List<ZoneEntity>> = fishingContext.activeMap.flatMapLatest { map ->
        if (map == null) flowOf(emptyList()) else dao.getZones(map.uid)
    }

    /** Секторы всех зон выбранного района. */
    val activeSectors: Flow<List<SectorEntity>> = activeZones.flatMapLatest { zones ->
        if (zones.isEmpty()) flowOf(emptyList()) else dao.getSectors(zones.map { it.uid })
    }

    suspend fun addZone(zone: ZoneEntity) = dao.insertZone(zone)

    suspend fun addSector(sector: SectorEntity) = dao.insertSector(sector)

    /** Удаление зоны уносит её секторы: без контура они висят в пустоте. */
    suspend fun deleteZone(zone: ZoneEntity) {
        dao.deleteSectorsOf(zone.uid)
        dao.deleteZone(zone)
    }
}

/**
 * Лежит ли точка внутри контура — трассировка луча.
 *
 * Имя не `contains`: у списка уже есть такой метод, он проверяет
 * равенство элементов и молча перебивал бы расширение.
 *
 * Нужна, чтобы понять, во что попал новый контур: обведённый кусок внутри
 * зоны — это её сектор, а не вторая зона поверх первой.
 */
fun List<GeoPoint>.containsPoint(point: GeoPoint): Boolean {
    if (size < 3) return false

    var inside = false
    var j = lastIndex
    for (i in indices) {
        val a = this[i]
        val b = this[j]
        // Луч пускается на запад: считаем, сколько раз он пересёк контур.
        // Пересечения ищутся по широте, а сравнение идёт по долготе — иначе
        // оси перепутаются и внутреннее станет внешним.
        val crossesLatitude = (a.latitude > point.latitude) != (b.latitude > point.latitude)
        if (crossesLatitude) {
            val longitudeAtCrossing = a.longitude +
                (point.latitude - a.latitude) * (b.longitude - a.longitude) /
                (b.latitude - a.latitude)
            if (point.longitude < longitudeAtCrossing) inside = !inside
        }
        j = i
    }
    return inside
}

/** Зона, внутрь которой целиком попал новый контур; null — свободное место. */
fun List<ZoneEntity>.enclosing(outline: List<GeoPoint>): ZoneEntity? = firstOrNull { zone ->
    val zoneOutline = zone.outline.decodeOutline()
    outline.all { zoneOutline.containsPoint(it) }
}
