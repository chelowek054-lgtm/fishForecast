package com.example.fishforecast.data.repository

import com.example.fishforecast.data.local.dao.ObservationDao
import com.example.fishforecast.data.local.entities.ObservationEntity
import com.example.fishforecast.domain.bite.ActiveObservation
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Что рыболов видел своими глазами на этом водоёме.
 *
 * Отметки живут у района: бой малька в соседнем пруду к этой рыбалке
 * отношения не имеет. Срок жизни хранится не здесь, а в словаре знаний —
 * поэтому репозиторий отдаёт всё, что записано, а отсеивает выдохшееся уже
 * расчёт, зная сроки.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ObservationRepository @Inject constructor(
    private val dao: ObservationDao,
    private val fishingContext: FishingContextRepository
) {
    /** Отметки выбранного района, свежие сверху. */
    val active: Flow<List<ObservationEntity>> = fishingContext.activeMap.flatMapLatest { map ->
        if (map == null) flowOf(emptyList()) else dao.getObservations(map.id)
    }

    suspend fun note(typeId: String) {
        val map = fishingContext.currentMap() ?: return
        dao.deleteOlderThan(System.currentTimeMillis() - KEEP_MILLIS)
        dao.insert(ObservationEntity(mapId = map.id, typeId = typeId))
    }

    suspend fun remove(observation: ObservationEntity) = dao.delete(observation)
}

/**
 * Отметки вместе со своими типами из словаря. Неизвестный тип пропускается:
 * словарь мог обновиться и потерять наблюдение, а расчёт от этого падать не
 * должен.
 */
fun List<ObservationEntity>.withTypes(catalog: KnowledgeCatalog): List<ActiveObservation> =
    mapNotNull { entity ->
        catalog.observation(entity.typeId)?.let { type ->
            ActiveObservation(
                type = type,
                notedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entity.notedAt),
                    ZoneId.systemDefault()
                )
            )
        }
    }

/** Сутки с запасом перекрывают самый долгий срок в словаре. */
private const val KEEP_MILLIS = 24L * 60 * 60 * 1000
