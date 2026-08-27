package com.example.fishforecast.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Пользовательская граница на воде: контур залива, ямы, тростника или
 * береговой полосы.
 *
 * Карт глубин и границ водоёмов в открытых источниках нет, но рыболовы
 * знают свои водоёмы и рисуют их сами. Зона — это знание о месте, которым
 * есть смысл делиться: она переживает обмен пакетами и в общей базе
 * остаётся одной и той же благодаря [uid].
 */
@Entity(tableName = "zones", indices = [Index("mapUid")])
data class ZoneEntity(
    /**
     * Глобальный идентификатор. Числовой ключ Room у каждого устройства
     * свой, поэтому для обмена нужен собственный: по нему при импорте видно,
     * что зона уже есть, и её надо обновить, а не задвоить.
     */
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),
    /** К какому району относится — по глобальному идентификатору карты. */
    val mapUid: String,
    val name: String,
    /** [ZoneKind] строкой: перечисления в базе хрупки к переименованию. */
    val kind: String = ZoneKind.WATER.name,
    /** Вершины контура: «широта,долгота;широта,долгота». */
    val outline: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Что обводит контур. Вода и берег ведут себя по-разному: наветренный берег
 * определяется геометрией уреза, а глубина и прогрев — площадью воды.
 */
enum class ZoneKind {
    /** Вода: залив, яма, отмель, полоса тростника. */
    WATER,

    /** Берег: подход, поляна, place где можно встать. */
    SHORE
}

/**
 * Сектор внутри зоны.
 *
 * На платниках и в клубах места нумеруют, и разговор о рыбалке ведётся
 * секторами, а не координатами: «взял в третьем». Сектор — то же знание,
 * только детальнее зоны.
 */
@Entity(tableName = "zone_sectors", indices = [Index("zoneUid")])
data class SectorEntity(
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),
    val zoneUid: String,
    /** Обычно номер или короткое имя: «3», «A1», «У мостков». */
    val name: String,
    val outline: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
