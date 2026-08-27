package com.example.fishforecast.data.repository

import android.content.Context
import com.example.fishforecast.data.local.CatalogStore
import com.example.fishforecast.data.local.dao.FishDao
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.remote.FishCatalogApi
import com.example.fishforecast.domain.fish.CatalogFish
import com.example.fishforecast.domain.fish.FishCatalog
import com.example.fishforecast.domain.fish.FishCatalogCodec
import com.example.fishforecast.domain.fish.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Справочник видов.
 *
 * Источников два: файл в ассетах, который едет вместе с приложением, и
 * общий справочник на сервере — рыба одна и та же, а знания о ней
 * пополняются быстрее, чем выходят обновления приложения.
 *
 * Оба источника — один и тот же документ, и правило слияния одно: вид
 * узнаётся по своему `id`. Своё при этом не затирается: описание, которое
 * рыболов правил под себя, и виды, заведённые им вручную, остаются.
 */
@Singleton
class FishRepository @Inject constructor(
    private val fishDao: FishDao,
    private val api: FishCatalogApi,
    private val store: CatalogStore,
    @ApplicationContext private val context: Context
) {
    fun getAllFish(): Flow<List<FishEntity>> = fishDao.getAllFish()

    suspend fun getFishById(id: Int): FishEntity? = fishDao.getFishById(id)

    /** Адрес общего справочника; пусто — источник не задан. */
    val catalogUrl: Flow<String?> = store.catalogUrl

    val catalogVersion: Flow<Int> = store.catalogVersion

    suspend fun setCatalogUrl(url: String?) = store.setCatalogUrl(url)

    /**
     * Раскладывает встроенный справочник по базе, когда это нужно.
     *
     * Раньше это делалось только для пустой базы, и получалось скверно: с
     * новой версией приложения приезжал новый справочник, а рыболов
     * продолжал видеть старый — база-то не пуста. Теперь у файла считается
     * отпечаток, и обновлённый справочник применяется при первом же
     * запуске.
     *
     * Своё при этом не страдает: виды, заведённые рыболовом, не трогаются,
     * его описания сохраняются. А вот пороги вида справочник перезапишет —
     * он для того и обновляется.
     */
    suspend fun syncBuiltInCatalog(): Result<Int> = runCatching {
        val text = readAssetText()
        val fingerprint = text.hashCode()
        if (fishDao.allFish().isNotEmpty() && store.builtInFingerprint.first() == fingerprint) {
            return@runCatching 0
        }

        val changed = applyCatalog(FishCatalogCodec.decode(text).getOrThrow())
        store.setBuiltInFingerprint(fingerprint)
        changed
    }

    /**
     * Обновляет справочник с сервера.
     *
     * Версия сравнивается до записи: качать одно и то же при каждом
     * открытии экрана незачем, а рыболову важно видеть, что нового не
     * появилось, а не «обновлено» вхолостую.
     */
    suspend fun refreshCatalogFromServer(force: Boolean = false): Result<CatalogUpdate> =
        runCatching {
            val url = store.catalogUrl.first()
            require(!url.isNullOrBlank()) { "Адрес справочника не задан" }

            val text = api.getCatalog(url)
            val catalog = FishCatalogCodec.decode(text).getOrThrow()
            val known = store.catalogVersion.first()

            if (!force && catalog.version in 1..known) {
                return@runCatching CatalogUpdate(catalog.version, changed = 0, upToDate = true)
            }

            val changed = applyCatalog(catalog)
            store.setCatalogVersion(catalog.version)
            CatalogUpdate(catalog.version, changed = changed, upToDate = false)
        }

    /** Возвращает справочник к тому, что едет с приложением. */
    suspend fun restoreCatalogFromAssets(): Result<Int> = runCatching {
        val text = readAssetText()
        val changed = applyCatalog(FishCatalogCodec.decode(text).getOrThrow())
        store.setBuiltInFingerprint(text.hashCode())
        changed
    }

    private suspend fun readAssetText(): String = withContext(Dispatchers.IO) {
        context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
    }

    /**
     * Записывает справочник поверх текущего и возвращает число затронутых
     * видов.
     */
    private suspend fun applyCatalog(catalog: FishCatalog): Int {
        val local = fishDao.allFish()
        var changed = 0

        catalog.fish.forEach { incoming ->
            val existing = local.matching(incoming)
            val entity = incoming.toEntity(existing)
            if (existing == null || existing != entity) {
                fishDao.insertFish(entity)
                changed++
            }
        }
        return changed
    }

    /**
     * Тот же вид в базе.
     *
     * Сначала по идентификатору — так и должно быть. Но у видов, заведённых
     * до появления общего справочника, идентификатор случайный, и находить
     * их приходится по имени, иначе после первого же обновления рыболов
     * получил бы двух щук.
     */
    private fun List<FishEntity>.matching(incoming: CatalogFish): FishEntity? =
        firstOrNull { it.uid == incoming.id }
            ?: firstOrNull { local -> local.name.matchesName(incoming.name) }

    suspend fun insertFish(fish: FishEntity) = fishDao.insertFish(fish)

    suspend fun deleteFish(fish: FishEntity) = fishDao.deleteFish(fish)

    private companion object {
        const val ASSET_NAME = "initial_fish.json"
    }
}

/** Что дало обновление справочника. */
data class CatalogUpdate(
    val version: Int,
    val changed: Int,
    val upToDate: Boolean
)

/**
 * Совпадают ли названия. В справочнике вид может называться через косую —
 * «Карп / Сазан», — и старая запись «Карп» это он же.
 */
internal fun String.matchesName(other: String): Boolean {
    fun parts(value: String) = value.split('/', ',')
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }

    val mine = parts(this)
    val theirs = parts(other)
    return mine.any { it in theirs }
}
