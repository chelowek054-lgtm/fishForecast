package com.example.fishforecast.data.repository

import android.content.Context
import com.example.fishforecast.data.local.CatalogStore
import com.example.fishforecast.data.remote.FishCatalogApi
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.domain.knowledge.KnowledgeCodec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Словари знаний: типы водоёмов, структуры, наблюдения.
 *
 * Отличие от справочника видов только в способе хранения. Виды лежат в Room
 * построчно: их правит рыболов, на них ссылаются точки и уловы. Словари —
 * документ целиком: их не редактируют по одному значению, поэтому скачанный
 * файл хранится как есть, а встроенный лежит в ассетах и служит запасом.
 *
 * Если скачанный документ окажется негодным, приложение молча возвращается
 * к встроенному: остаться без словарей значит остаться без расчёта.
 */
@Singleton
class KnowledgeRepository @Inject constructor(
    private val api: FishCatalogApi,
    private val store: CatalogStore,
    @ApplicationContext private val context: Context
) {
    /** Действующие словари: скачанные, а если их нет — встроенные. */
    val catalog: Flow<KnowledgeCatalog> = store.knowledgeDocument.map { saved ->
        saved?.let { KnowledgeCodec.decode(it).getOrNull() } ?: builtIn()
    }

    val knowledgeUrl: Flow<String?> = store.knowledgeUrl

    val knowledgeVersion: Flow<Int> = store.knowledgeVersion

    suspend fun current(): KnowledgeCatalog = catalog.first()

    suspend fun setKnowledgeUrl(url: String?) = store.setKnowledgeUrl(url)

    /**
     * Скачивает словари. Версия сравнивается до записи: словари меняются
     * редко, и незачем перезаписывать их при каждом открытии экрана.
     */
    suspend fun refreshFromServer(force: Boolean = false): Result<KnowledgeUpdate> = runCatching {
        val url = store.knowledgeUrl.first()
        require(!url.isNullOrBlank()) { "Адрес словарей не задан" }

        val text = api.getCatalog(url)
        val incoming = KnowledgeCodec.decode(text).getOrThrow()
        val known = store.knowledgeVersion.first()

        if (!force && incoming.version in 1..known) {
            return@runCatching KnowledgeUpdate(incoming.version, upToDate = true)
        }

        store.setKnowledgeDocument(text, incoming.version)
        KnowledgeUpdate(incoming.version, upToDate = false)
    }

    /** Возвращает словари к тем, что едут с приложением. */
    suspend fun restoreBuiltIn(): Result<Unit> = runCatching {
        store.setKnowledgeDocument(null, 0)
    }

    private suspend fun builtIn(): KnowledgeCatalog = withContext(Dispatchers.IO) {
        cached ?: run {
            val text = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            KnowledgeCodec.decode(text).getOrThrow().also { cached = it }
        }
    }

    /** Встроенные словари не меняются на ходу — читать файл каждый раз незачем. */
    @Volatile
    private var cached: KnowledgeCatalog? = null

    private companion object {
        const val ASSET_NAME = "knowledge.json"
    }
}

data class KnowledgeUpdate(
    val version: Int,
    val upToDate: Boolean
)
