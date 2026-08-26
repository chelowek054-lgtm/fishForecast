package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.ActiveMapStore
import com.example.fishforecast.data.local.dao.SavedMapDao
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Контекст рыбалки: выбранная карта и всё, что из неё следует.
 *
 * Раньше каждый экран сам добывал координаты через GPS и читал общий на всё
 * приложение прогноз. Теперь источник один — активная карта: по её центру
 * запрашивается погода, её границы отбирают точки, её норма давления идёт в
 * расчёт клёва.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FishingContextRepository @Inject constructor(
    private val savedMapDao: SavedMapDao,
    private val activeMapStore: ActiveMapStore,
    private val weatherRepository: WeatherRepository,
    private val spotRepository: FishingSpotRepository
) {
    val savedMaps: Flow<List<SavedMapEntity>> = savedMapDao.getRegions()

    /**
     * Выбранная карта. Если выбор указывает на удалённую карту или его ещё
     * не было, подставляется самая свежая — иначе приложение молча осталось
     * бы без контекста после удаления.
     */
    val activeMap: Flow<SavedMapEntity?> =
        combine(activeMapStore.activeMapId, savedMaps) { id, maps ->
            maps.firstOrNull { it.id == id } ?: maps.firstOrNull()
        }

    /** Прогноз выбранной карты; пусто, пока карты нет. */
    val activeForecast: Flow<List<WeatherEntity>> = activeMap.flatMapLatest { map ->
        if (map == null) flowOf(emptyList()) else weatherRepository.forecastForMap(map.id)
    }

    /**
     * Точки внутри границ карты. Принадлежность определяется геометрией, а не
     * ссылкой: тогда точки, импортированные из чужого GPX, тоже находятся.
     */
    val activeSpots: Flow<List<FishingSpotEntity>> = activeMap.flatMapLatest { map ->
        if (map == null) {
            flowOf(emptyList())
        } else {
            spotRepository.spots.map { spots ->
                spots.filter { map.contains(it.latitude, it.longitude) }
            }
        }
    }

    suspend fun setActiveMap(id: Int) = activeMapStore.setActiveMapId(id)

    /** Делает активной последнюю сохранённую карту. */
    suspend fun activateLatestMap() {
        savedMapDao.getLatestMap()?.let { activeMapStore.setActiveMapId(it.id) }
    }

    suspend fun renameMap(id: Int, name: String) = savedMapDao.rename(id, name)

    suspend fun setNormalPressure(id: Int, normalPressureMmHg: Double?) =
        savedMapDao.updateNormalPressure(id, normalPressureMmHg)

    /** Норма точки уточняет норму карты; без обеих остаётся null. */
    fun normalPressureFor(map: SavedMapEntity?, spot: FishingSpotEntity?): Double? =
        spot?.normalPressureMmHg ?: map?.normalPressureMmHg

    suspend fun currentMap(): SavedMapEntity? = activeMap.first()

    /** Обновляет прогноз выбранной карты по её центру. */
    suspend fun refreshWeather(): Result<Unit> {
        val map = currentMap()
            ?: return Result.failure(IllegalStateException("Сначала сохраните карту района"))

        return weatherRepository.fetchWeather(
            mapId = map.id,
            lat = map.centerLatitude,
            lon = map.centerLongitude
        )
    }
}
