package com.example.fishforecast.data.repository

import android.app.Application
import android.net.Uri
import com.example.fishforecast.data.local.ActiveMapStore
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.dao.FishingSpotDao
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.dao.ZoneDao
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.domain.share.PackAuthor
import com.example.fishforecast.domain.share.RegionPack
import com.example.fishforecast.domain.share.RegionPackCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обмен районами.
 *
 * Пакет — это знание о месте: границы, норма давления, глубины, обведённые
 * зоны с секторами, точки и виды рыб, которые здесь берут. Тайлы в него не
 * входят: получатель докачает их сам по границам района, файл остаётся в
 * килобайтах, и чужие тайлы никуда не раздаются.
 *
 * Всё связано глобальными идентификаторами, поэтому один и тот же район,
 * пришедший дважды или от разных людей, остаётся одним районом. Это же
 * правило будет действовать на общем сервере — см. `app/docs/RegionPack.md`.
 */
@Singleton
class RegionPackRepository @Inject constructor(
    private val application: Application,
    private val savedMapDao: SavedMapDao,
    private val spotDao: FishingSpotDao,
    private val fishDao: FishDao,
    private val zoneDao: ZoneDao,
    private val store: ActiveMapStore
) {

    /** Сколько чего приехало: рыболову важно понимать, что он получил. */
    data class ImportSummary(
        val regionName: String,
        val regionAdded: Boolean,
        val zonesAdded: Int,
        val sectorsAdded: Int,
        val spotsAdded: Int,
        val fishAdded: Int,
        val needsTiles: Boolean
    )

    /**
     * Собирает пакет района в файл кэша.
     *
     * В пакет попадают точки внутри границ, а не все подряд: район — это
     * место, и чужие точки с другого водоёма получателю ни к чему.
     */
    suspend fun exportRegion(mapId: Int): Result<File> = runCatching {
        val map = savedMapDao.getRegionById(mapId) ?: error("Район не найден")
        val zones = zoneDao.zonesOf(map.uid)
        val sectors = zoneDao.sectorsOf(zones.map { it.uid })
        val spots = spotDao.allSpots().filter { map.contains(it.latitude, it.longitude) }
        val allFish = fishDao.allFish()
        // Справочник уезжает не целиком: только те виды, которые привязаны к
        // точкам этого района. Остальное — знание о других водоёмах.
        val fish = allFish.filter { entry -> spots.any { it.fishId == entry.id } }

        val pack = RegionPackCodec.build(
            map = map,
            zones = zones,
            sectors = sectors,
            spots = spots,
            fish = fish,
            packId = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now().format(TIMESTAMP),
            author = PackAuthor(
                id = store.ensureAuthorId(),
                name = store.authorName.first()
            )
        )

        withContext(Dispatchers.IO) {
            val directory = File(application.cacheDir, "shared").apply { mkdirs() }
            val file = File(directory, "${map.name.toFileName()}.${RegionPack.FILE_EXTENSION}")
            file.writeText(RegionPackCodec.encode(pack))
            file
        }
    }

    /**
     * Принимает чужой пакет.
     *
     * Уже известное обновляется, незнакомое заводится. Правило одно и то же
     * для всего содержимого: совпал глобальный идентификатор — это та же
     * запись, что бы ни было написано в имени.
     */
    suspend fun importPack(uri: Uri): Result<ImportSummary> = runCatching {
        val text = withContext(Dispatchers.IO) {
            application.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                ?: error("Не удалось прочитать файл")
        }

        val pack = RegionPackCodec.decode(text).getOrThrow()
        val contents = RegionPackCodec.toEntities(pack)

        val existingMap = savedMapDao.getRegionByUid(contents.map.uid)
        val mapId = if (existingMap == null) {
            savedMapDao.insertRegion(contents.map).toInt()
        } else {
            // Своя скачанная область и её размер остаются: это про тайлы на
            // устройстве, а не про знание о месте.
            savedMapDao.insertRegion(
                contents.map.copy(
                    id = existingMap.id,
                    offlineRegionId = existingMap.offlineRegionId,
                    sizeBytes = existingMap.sizeBytes,
                    // Свой замер воды точнее чужого: он сделан здесь.
                    waterTempC = existingMap.waterTempC ?: contents.map.waterTempC,
                    waterTempAt = existingMap.waterTempAt ?: contents.map.waterTempAt
                )
            )
            existingMap.id
        }

        var fishAdded = 0
        val fishIdByUid = mutableMapOf<String, Int>()
        contents.fish.forEach { incoming ->
            val known = fishDao.getFishByUid(incoming.uid)
            if (known == null) {
                fishDao.insertFish(incoming)
                fishAdded++
                fishDao.getFishByUid(incoming.uid)?.let { fishIdByUid[incoming.uid] = it.id }
            } else {
                // Чужие пороги не переписывают свои: справочник рыболов
                // правит под себя, и это его работа.
                fishIdByUid[incoming.uid] = known.id
            }
        }

        var zonesAdded = 0
        contents.zones.forEach { zone ->
            zonesAdded++
            zoneDao.insertZone(zone)
        }
        var sectorsAdded = 0
        contents.sectors.forEach { sector ->
            sectorsAdded++
            zoneDao.insertSector(sector)
        }

        var spotsAdded = 0
        contents.spots.forEach { spot ->
            val known = spotDao.getSpotByUid(spot.uid)
            val fishId = contents.spotFish[spot.uid]?.let { fishIdByUid[it] }
            if (known == null) spotsAdded++
            spotDao.insertSpot(spot.copy(id = known?.id ?: 0, fishId = fishId))
        }

        ImportSummary(
            regionName = contents.map.name,
            regionAdded = existingMap == null,
            zonesAdded = zonesAdded,
            sectorsAdded = sectorsAdded,
            spotsAdded = spotsAdded,
            fishAdded = fishAdded,
            // Тайлов в пакете нет: без сети чужой район открыть не выйдет.
            needsTiles = savedMapDao.getRegionById(mapId)?.sizeBytes == 0L
        )
    }

    private fun String.toFileName(): String =
        lowercase().map { if (it.isLetterOrDigit()) it else '_' }.joinToString("").trim('_')
            .ifBlank { "region" }

    private companion object {
        val TIMESTAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }
}

/** Точки района: помогает и экспорту, и экрану списка. */
fun List<FishingSpotEntity>.withFish(fish: List<FishEntity>): Map<FishingSpotEntity, FishEntity?> =
    associateWith { spot -> fish.firstOrNull { it.id == spot.fishId } }
