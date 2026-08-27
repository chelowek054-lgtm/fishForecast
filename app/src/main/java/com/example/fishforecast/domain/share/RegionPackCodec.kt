package com.example.fishforecast.domain.share

import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.SectorEntity
import com.example.fishforecast.data.local.entities.ZoneEntity
import com.example.fishforecast.domain.fish.decodeBaits
import com.example.fishforecast.domain.fish.encodeBaits
import com.example.fishforecast.domain.fish.toCatalogFish
import com.example.fishforecast.domain.fish.toEntity
import kotlinx.serialization.json.Json

/**
 * Перевод между тем, что лежит в базе, и тем, что уходит наружу.
 *
 * Граница проведена намеренно: внутренние сущности меняются вместе с
 * приложением, а формат обмена — общий с другими устройствами и с будущим
 * сервером, и ломать его нельзя. Всё, что знает про версии и совместимость,
 * живёт здесь.
 */
object RegionPackCodec {

    /**
     * Незнакомые поля игнорируются: пакет из более новой версии приложения
     * должен читаться, если основная часть схемы не менялась. Отсутствующие
     * поля берут значения по умолчанию — так же переживаются пакеты из
     * версий постарше.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun encode(pack: RegionPack): String = json.encodeToString(RegionPack.serializer(), pack)

    /**
     * Читает пакет и проверяет схему.
     *
     * Отказ лучше молчаливой порчи: если формат сменит поколение, старое
     * приложение не должно делать вид, что поняло, и заводить у себя район с
     * половиной данных.
     */
    fun decode(text: String): Result<RegionPack> = runCatching {
        val pack = json.decodeFromString(RegionPack.serializer(), text)
        val incoming = pack.schema.substringAfterLast('/')
        val supported = RegionPack.SCHEMA.substringAfterLast('/')
        require(pack.schema.substringBeforeLast('/') == RegionPack.SCHEMA.substringBeforeLast('/')) {
            "Это не пакет района FishForecast"
        }
        require(incoming == supported) {
            "Пакет собран новой версией приложения (схема $incoming, поддерживается $supported)"
        }
        pack
    }

    /** Собирает пакет из того, что накопилось у рыболова по этому району. */
    fun build(
        map: SavedMapEntity,
        zones: List<ZoneEntity>,
        sectors: List<SectorEntity>,
        spots: List<FishingSpotEntity>,
        fish: List<FishEntity>,
        packId: String,
        createdAt: String,
        author: PackAuthor?
    ): RegionPack {
        val sectorsByZone = sectors.groupBy { it.zoneUid }

        return RegionPack(
            id = packId,
            createdAt = createdAt,
            author = author,
            region = PackRegion(
                id = map.uid,
                name = map.name,
                bounds = PackBounds(map.north, map.south, map.east, map.west),
                minZoom = map.minZoom,
                maxZoom = map.maxZoom,
                normalPressureMmHg = map.baselinePressureMmHg,
                elevationM = map.elevationM,
                waterBodyType = map.waterBodyType,
                shallowDepthM = map.shallowDepthM,
                deepDepthM = map.deepDepthM
            ),
            zones = zones.map { zone ->
                PackZone(
                    id = zone.uid,
                    name = zone.name,
                    kind = zone.kind,
                    outline = zone.outline.decodeOutline().map { PackPoint(it.latitude, it.longitude) },
                    note = zone.note,
                    sectors = sectorsByZone[zone.uid].orEmpty().map { sector ->
                        PackSector(
                            id = sector.uid,
                            name = sector.name,
                            outline = sector.outline.decodeOutline()
                                .map { PackPoint(it.latitude, it.longitude) },
                            note = sector.note
                        )
                    }
                )
            },
            spots = spots.map { spot ->
                PackSpot(
                    id = spot.uid,
                    name = spot.name,
                    lat = spot.latitude,
                    lon = spot.longitude,
                    placement = spot.placement,
                    note = spot.note,
                    structures = spot.structures.decodeBaits(),
                    // Ссылка на вид — глобальная: числовой ключ справочника
                    // у получателя свой.
                    fishId = fish.firstOrNull { it.id == spot.fishId }?.uid,
                    zoneId = spot.zoneUid,
                    sectorId = spot.sectorUid
                )
            },
            fish = fish.map { it.toCatalogFish() }
        )
    }

    /**
     * Разбирает пакет в сущности.
     *
     * Числовые ключи не проставляются: их знает только та база, куда пакет
     * приедет. Здесь всё связано глобальными идентификаторами — по ним
     * репозиторий и решает, что обновить, а что завести заново.
     */
    fun toEntities(pack: RegionPack): RegionPackContents = RegionPackContents(
        map = SavedMapEntity(
            uid = pack.region.id,
            name = pack.region.name,
            // Область в MapLibre у получателя будет своя: тайлы не
            // передаются, их докачают по этим границам.
            offlineRegionId = 0,
            north = pack.region.bounds.north,
            south = pack.region.bounds.south,
            east = pack.region.bounds.east,
            west = pack.region.bounds.west,
            minZoom = pack.region.minZoom,
            maxZoom = pack.region.maxZoom,
            baselinePressureMmHg = pack.region.normalPressureMmHg,
            elevationM = pack.region.elevationM,
            waterBodyType = pack.region.waterBodyType,
            shallowDepthM = pack.region.shallowDepthM,
            deepDepthM = pack.region.deepDepthM
        ),
        zones = pack.zones.map { zone ->
            ZoneEntity(
                uid = zone.id,
                mapUid = pack.region.id,
                name = zone.name,
                kind = zone.kind,
                outline = zone.outline.map { GeoPoint(it.lat, it.lon) }.encodeOutline(),
                note = zone.note
            )
        },
        sectors = pack.zones.flatMap { zone ->
            zone.sectors.map { sector ->
                SectorEntity(
                    uid = sector.id,
                    zoneUid = zone.id,
                    name = sector.name,
                    outline = sector.outline.map { GeoPoint(it.lat, it.lon) }.encodeOutline(),
                    note = sector.note
                )
            }
        },
        spots = pack.spots.map { spot ->
            FishingSpotEntity(
                uid = spot.id,
                name = spot.name,
                latitude = spot.lat,
                longitude = spot.lon,
                note = spot.note,
                placement = spot.placement,
                structures = spot.structures.encodeBaits(),
                zoneUid = spot.zoneId,
                sectorUid = spot.sectorId
            )
        },
        // Привязка точки к виду хранится отдельно: числовой ключ рыбы
        // станет известен только после того, как справочник получателя
        // примет чужие виды.
        spotFish = pack.spots.mapNotNull { spot -> spot.fishId?.let { spot.id to it } }.toMap(),
        fish = pack.fish.map { it.toEntity() }
    )
}

/** Содержимое пакета, разобранное в сущности приложения. */
data class RegionPackContents(
    val map: SavedMapEntity,
    val zones: List<ZoneEntity>,
    val sectors: List<SectorEntity>,
    val spots: List<FishingSpotEntity>,
    /** Точка (uid) → вид рыбы (uid). */
    val spotFish: Map<String, String>,
    val fish: List<FishEntity>
)
