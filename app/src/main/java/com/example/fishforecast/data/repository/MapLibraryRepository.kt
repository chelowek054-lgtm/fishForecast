package com.example.fishforecast.data.repository

import android.app.Application
import android.net.Uri
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.entities.SavedMapEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import org.maplibre.android.storage.FileSource
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Обмен скачанными картами. MapLibre хранит все области в одной базе и не
 * умеет выгружать их поштучно, поэтому наружу уходит вся база целиком, а
 * при импорте чужие области подмешиваются к своим через mergeOfflineRegions.
 */
@Singleton
class MapLibraryRepository @Inject constructor(
    private val application: Application,
    private val dao: SavedMapDao
) {
    private val offlineManager: OfflineManager
        get() = OfflineManager.getInstance(application)

    /**
     * MapLibre держит базу в каталоге ресурсов, но путь зависит от того,
     * успел ли инициализироваться FileSource, поэтому проверяются оба места.
     */
    private val offlineDatabase: File?
        get() = listOf(
            runCatching { FileSource.getResourcesCachePath(application) }.getOrNull(),
            application.filesDir.absolutePath
        )
            .filterNotNull()
            .map { File(it, OFFLINE_DB_NAME) }
            .firstOrNull { it.exists() }

    /** Готовит копию базы карт для отправки. Возвращает файл в кэше. */
    suspend fun exportMaps(): Result<File> = runCatching {
        // Упаковка убирает страницы, освободившиеся после удалений: без неё
        // файл уезжает получателю раздутым. Колбэки MapLibre приходят только
        // на главный поток, поэтому сам вызов делается там, а копирование —
        // уже на IO.
        withContext(Dispatchers.Main) { packDatabase() }

        withContext(Dispatchers.IO) {
            val source = offlineDatabase ?: error("Скачанных карт пока нет")
            val target = File(File(application.cacheDir, "shared").apply { mkdirs() }, EXPORT_NAME)
            source.copyTo(target, overwrite = true)
            target
        }
    }

    /**
     * Принимает чужой файл карт. Области, которых ещё нет, попадают и в
     * MapLibre, и в список приложения.
     */
    suspend fun importMaps(uri: Uri): Result<Int> = runCatching {
        val incoming = withContext(Dispatchers.IO) {
            val file = File(application.cacheDir, "incoming_maps.db")
            application.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use(input::copyTo)
            } ?: error("Не удалось прочитать файл")
            file
        }

        // Слияние — вызов MapLibre, его колбэк приходит на главный поток.
        val merged = withContext(Dispatchers.Main) { mergeRegions(incoming.absolutePath) }
        incoming.delete()

        val known = dao.getKnownRegionIds().toSet()
        var added = 0
        merged.forEach { region ->
            if (region.id !in known) {
                // Размер известен только самому MapLibre — без него чужая
                // карта висела бы в списке как «0 КБ».
                val size = withContext(Dispatchers.Main) { regionSize(region) }
                dao.insertRegion(region.toEntity(size))
                added++
            }
        }
        added
    }

    private suspend fun regionSize(region: OfflineRegion): Long =
        suspendCancellableCoroutine { continuation ->
            region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                override fun onStatus(status: OfflineRegionStatus?) {
                    continuation.resume(status?.completedResourceSize ?: 0L)
                }

                override fun onError(error: String?) {
                    continuation.resume(0L)
                }
            })
        }

    private suspend fun mergeRegions(path: String): List<OfflineRegion> =
        suspendCancellableCoroutine { continuation ->
            offlineManager.mergeOfflineRegions(
                path,
                object : OfflineManager.MergeOfflineRegionsCallback {
                    override fun onMerge(offlineRegions: Array<OfflineRegion>?) {
                        continuation.resume(offlineRegions?.toList().orEmpty())
                    }

                    override fun onError(error: String) {
                        continuation.cancel(IllegalStateException(error))
                    }
                }
            )
        }

    private suspend fun packDatabase() = suspendCancellableCoroutine { continuation ->
        offlineManager.packDatabase(object : OfflineManager.FileSourceCallback {
            override fun onSuccess() {
                continuation.resume(Unit)
            }

            override fun onError(message: String) {
                // Упаковка — оптимизация: без неё файл больше, но пригоден.
                continuation.resume(Unit)
            }
        })
    }

    private fun OfflineRegion.toEntity(sizeBytes: Long): SavedMapEntity {
        val definition = definition as? OfflineTilePyramidRegionDefinition
        val bounds = definition?.bounds

        return SavedMapEntity(
            name = metadata.decodeToString().ifBlank { "Импортированная карта" },
            offlineRegionId = id,
            north = bounds?.latitudeNorth ?: 0.0,
            south = bounds?.latitudeSouth ?: 0.0,
            east = bounds?.longitudeEast ?: 0.0,
            west = bounds?.longitudeWest ?: 0.0,
            minZoom = definition?.minZoom ?: 0.0,
            maxZoom = definition?.maxZoom ?: 0.0,
            sizeBytes = sizeBytes
        )
    }

    companion object {
        private const val OFFLINE_DB_NAME = "mbgl-offline.db"
        const val EXPORT_NAME = "fishforecast_maps.db"
    }
}
