package com.example.fishforecast.data.repository

import android.app.Application
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.ui.map.MapConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Ход скачивания одной области. */
sealed interface RegionDownloadState {
    data class InProgress(val percent: Int) : RegionDownloadState
    data class Done(val sizeBytes: Long) : RegionDownloadState
    data class Failed(val message: String) : RegionDownloadState
}

@Singleton
class OfflineMapRepository @Inject constructor(
    private val application: Application,
    private val dao: SavedMapDao
) {
    val regions: Flow<List<SavedMapEntity>> = dao.getRegions()

    private val offlineManager: OfflineManager
        get() = OfflineManager.getInstance(application)

    /**
     * Скачивает рамку, которую рыболов оставил на экране. Поток живёт до
     * завершения загрузки; отписка останавливает её, а не отменяет — область
     * остаётся частично скачанной и продолжится при следующем запуске.
     */
    fun downloadRegion(
        name: String,
        bounds: LatLngBounds,
        minZoom: Double,
        maxZoom: Double,
        normalPressureMmHg: Double? = null
    ): Flow<RegionDownloadState> = callbackFlow {
        // MapLibre отвечает на негодное определение области java.lang.Error,
        // а не исключением, — без перехвата это мгновенный краш приложения
        // прямо посреди рыбалки.
        val definition = runCatching {
            require(bounds.latitudeNorth > bounds.latitudeSouth) { "Пустая рамка по широте" }
            require(minZoom <= maxZoom) { "Масштабы перепутаны: $minZoom > $maxZoom" }

            OfflineTilePyramidRegionDefinition(
                MapConfig.STYLE_URL,
                bounds,
                minZoom,
                maxZoom,
                application.resources.displayMetrics.density
            )
        }.getOrElse { error ->
            trySend(
                RegionDownloadState.Failed(
                    "Не удалось описать область: ${error.message ?: "неверные границы"}"
                )
            )
            close()
            return@callbackFlow
        }

        runCatching {
        offlineManager.createOfflineRegion(
            definition,
            name.toByteArray(),
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                        override fun onStatusChanged(status: OfflineRegionStatus) {
                            val required = status.requiredResourceCount
                            val percent = if (required > 0) {
                                (status.completedResourceCount * 100 / required).toInt()
                            } else {
                                0
                            }

                            if (status.isComplete) {
                                launch {
                                    dao.updateSize(offlineRegion.id, status.completedResourceSize)
                                }
                                trySend(RegionDownloadState.Done(status.completedResourceSize))
                                offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                                close()
                            } else {
                                trySend(RegionDownloadState.InProgress(percent.coerceIn(0, 100)))
                            }
                        }

                        override fun onError(error: OfflineRegionError) {
                            trySend(RegionDownloadState.Failed("${error.reason}: ${error.message}"))
                            close()
                        }

                        override fun mapboxTileCountLimitExceeded(limit: Long) {
                            trySend(
                                RegionDownloadState.Failed(
                                    "Область слишком большая: лимит $limit тайлов. " +
                                        "Уменьшите масштаб или выберите участок поменьше."
                                )
                            )
                            close()
                        }
                    })

                    // Область записывается сразу: если загрузка прервётся,
                    // она останется в списке и её можно будет удалить.
                    launch {
                        dao.insertRegion(
                            SavedMapEntity(
                                name = name,
                                offlineRegionId = offlineRegion.id,
                                north = bounds.latitudeNorth,
                                south = bounds.latitudeSouth,
                                east = bounds.longitudeEast,
                                west = bounds.longitudeWest,
                                minZoom = minZoom,
                                maxZoom = maxZoom,
                                normalPressureMmHg = normalPressureMmHg
                            )
                        )
                    }
                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                }

                override fun onError(error: String) {
                    trySend(RegionDownloadState.Failed(error))
                    close()
                }
            }
        )
        }.onFailure { error ->
            trySend(RegionDownloadState.Failed(error.message ?: "Не удалось начать загрузку"))
            close()
        }

        awaitClose { }
    }

    /** Удаляет и запись, и скачанные тайлы — иначе база MapLibre растёт молча. */
    suspend fun deleteRegion(region: SavedMapEntity) {
        dao.deleteRegion(region)
        deleteOfflineTiles(region.offlineRegionId)
    }

    private suspend fun deleteOfflineTiles(offlineRegionId: Long) =
        suspendCancellableCoroutine { continuation ->
            offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
                override fun onList(offlineRegions: Array<OfflineRegion>?) {
                    val target = offlineRegions?.firstOrNull { it.id == offlineRegionId }
                    if (target == null) {
                        continuation.resume(Unit)
                        return
                    }
                    target.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                        override fun onDelete() {
                            continuation.resume(Unit)
                        }

                        override fun onError(error: String) {
                            continuation.resume(Unit)
                        }
                    })
                }

                override fun onError(error: String) {
                    continuation.resume(Unit)
                }
            })
        }
}
