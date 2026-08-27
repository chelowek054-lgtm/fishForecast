package com.example.fishforecast.data.remote

import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Общий справочник видов с сервера.
 *
 * Адрес задаёт рыболов, поэтому запрос идёт по полному URL, а не по пути
 * относительно базового: сервера у проекта пока нет, а справочник уже
 * можно держать хоть в репозитории, хоть на своём хостинге.
 *
 * Ответ забирается строкой: разбором и проверкой схемы занимается
 * [com.example.fishforecast.domain.fish.FishCatalogCodec], и он один и тот
 * же для файла в ассетах и для сети.
 */
interface FishCatalogApi {

    @GET
    suspend fun getCatalog(@Url url: String): String
}
