package com.example.fishforecast.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.catalogSettings by preferencesDataStore(name = "fish_catalog")

/**
 * Откуда брать общий справочник видов и что уже скачано.
 *
 * Это настройка, а не данные: сами виды живут в Room. Версия хранится
 * рядом с адресом, потому что она свойство именно этого источника —
 * сменился адрес, обнуляется и версия.
 */
@Singleton
class CatalogStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val catalogUrl: Flow<String?> = context.catalogSettings.data.map { it[CATALOG_URL] }

    val catalogVersion: Flow<Int> = context.catalogSettings.data.map { it[CATALOG_VERSION] ?: 0 }

    suspend fun setCatalogUrl(url: String?) {
        context.catalogSettings.edit { preferences ->
            if (url.isNullOrBlank()) {
                preferences.remove(CATALOG_URL)
            } else {
                preferences[CATALOG_URL] = url.trim()
            }
            // Новый источник — своя нумерация версий, старая ничего не значит.
            preferences.remove(CATALOG_VERSION)
        }
    }

    /**
     * Отпечаток встроенного справочника, который уже разложен по базе.
     * По нему видно, что файл в приложении обновился и его надо применить.
     */
    val builtInFingerprint: Flow<Int> = context.catalogSettings.data
        .map { it[BUILT_IN_FINGERPRINT] ?: 0 }

    suspend fun setBuiltInFingerprint(fingerprint: Int) {
        context.catalogSettings.edit { it[BUILT_IN_FINGERPRINT] = fingerprint }
    }

    suspend fun setCatalogVersion(version: Int) {
        context.catalogSettings.edit { preferences -> preferences[CATALOG_VERSION] = version }
    }

    /**
     * Скачанные словари знаний целиком, как пришли.
     *
     * Виды лежат в Room построчно: их правит рыболов, на них ссылаются
     * точки. Словари же документ — их не редактируют по одному значению и
     * не запрашивают выборками, поэтому хранить их таблицами незачем.
     */
    val knowledgeDocument: Flow<String?> = context.catalogSettings.data
        .map { it[KNOWLEDGE_DOCUMENT] }

    val knowledgeUrl: Flow<String?> = context.catalogSettings.data.map { it[KNOWLEDGE_URL] }

    val knowledgeVersion: Flow<Int> = context.catalogSettings.data
        .map { it[KNOWLEDGE_VERSION] ?: 0 }

    suspend fun setKnowledgeDocument(text: String?, version: Int) {
        context.catalogSettings.edit { preferences ->
            if (text == null) {
                preferences.remove(KNOWLEDGE_DOCUMENT)
                preferences.remove(KNOWLEDGE_VERSION)
            } else {
                preferences[KNOWLEDGE_DOCUMENT] = text
                preferences[KNOWLEDGE_VERSION] = version
            }
        }
    }

    suspend fun setKnowledgeUrl(url: String?) {
        context.catalogSettings.edit { preferences ->
            if (url.isNullOrBlank()) preferences.remove(KNOWLEDGE_URL) else {
                preferences[KNOWLEDGE_URL] = url.trim()
            }
            preferences.remove(KNOWLEDGE_VERSION)
        }
    }

    private companion object {
        val CATALOG_URL = stringPreferencesKey("catalog_url")
        val CATALOG_VERSION = intPreferencesKey("catalog_version")
        val BUILT_IN_FINGERPRINT = intPreferencesKey("built_in_fingerprint")
        val KNOWLEDGE_DOCUMENT = stringPreferencesKey("knowledge_document")
        val KNOWLEDGE_URL = stringPreferencesKey("knowledge_url")
        val KNOWLEDGE_VERSION = intPreferencesKey("knowledge_version")
    }
}
