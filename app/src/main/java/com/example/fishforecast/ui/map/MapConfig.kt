package com.example.fishforecast.ui.map

import com.example.fishforecast.BuildConfig

/** Что рисуется под точками: схема или снимок со спутника. */
enum class BaseLayer {
    SCHEME,
    SATELLITE;

    val title: String
        get() = when (this) {
            SCHEME -> "Схема"
            SATELLITE -> "Спутник"
        }
}

/**
 * Источник карты — единственное место, которое нужно поменять при переходе
 * на собственный эндпоинт тайлов: скачивание офлайн-областей подхватит
 * новый стиль само.
 *
 * Сейчас стоит OpenFreeMap (данные OSM, без ключей и квот). Демо-стиль
 * MapLibre использовать нельзя: на нём офлайн-загрузка роняет процесс
 * нативным `std::regex_error` внутри libmaplibre.
 *
 * Перед публикацией: подтвердить у выбранного источника право на
 * предзагрузку тайлов либо перейти на свой хостинг PMTiles (Protomaps в R2).
 * Атрибуция OpenStreetMap обязательна в любом случае.
 */
object MapConfig {
    const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

    /**
     * Спутниковый слой — Sentinel-2 cloudless (EOX), безоблачная мозаика
     * с открытой лицензией CC BY. Разрешение снимков 10 м на пиксель,
     * поэтому дальше 14-го масштаба тайлов просто нет — это ровно наш
     * потолок для офлайн-областей.
     *
     * Стиль задаётся JSON, а не ссылкой: сервис отдаёт тайлы, но не
     * готовый style-документ. Атрибуция обязательна по лицензии.
     */
    const val MAX_SATELLITE_ZOOM = 14

    /**
     * Детальные снимки берутся у провайдера по ключу — только они дают
     * разрешение около метра. Ключ лежит в local.properties и не попадает
     * в репозиторий; без него остаётся Sentinel-2, который работает всегда.
     *
     * Предзагрузка платных тайлов в офлайн запрещена условиями обоих
     * провайдеров, поэтому детальный слой доступен только при сети.
     */
    private val detailedSatellite: DetailedSatellite? = when {
        BuildConfig.MAPTILER_KEY.isNotBlank() -> DetailedSatellite(
            name = "MapTiler",
            tileUrl = "https://api.maptiler.com/tiles/satellite-v2/{z}/{x}/{y}.jpg" +
                "?key=${BuildConfig.MAPTILER_KEY}",
            maxZoom = 20,
            attribution = "© MapTiler © OpenStreetMap contributors"
        )

        BuildConfig.MAPBOX_TOKEN.isNotBlank() -> DetailedSatellite(
            name = "Mapbox",
            tileUrl = "https://api.mapbox.com/v4/mapbox.satellite/{z}/{x}/{y}@2x.jpg90" +
                "?access_token=${BuildConfig.MAPBOX_TOKEN}",
            maxZoom = 20,
            attribution = "© Mapbox © Maxar"
        )

        else -> null
    }

    /** Есть ли детальный источник: от этого зависит подпись на экране. */
    val hasDetailedSatellite: Boolean get() = detailedSatellite != null

    val satelliteProviderName: String get() = detailedSatellite?.name ?: "Sentinel-2"

    /** Стиль снимков: детальный, если ключ задан, иначе открытый Sentinel-2. */
    val satelliteStyleJson: String
        get() = detailedSatellite?.let { provider ->
            rasterStyle(
                sourceId = "satellite",
                tileUrl = provider.tileUrl,
                maxZoom = provider.maxZoom,
                attribution = provider.attribution
            )
        } ?: sentinelStyleJson

    private data class DetailedSatellite(
        val name: String,
        val tileUrl: String,
        val maxZoom: Int,
        val attribution: String
    )

    private fun rasterStyle(
        sourceId: String,
        tileUrl: String,
        maxZoom: Int,
        attribution: String
    ): String = """
        {
          "version": 8,
          "sources": {
            "$sourceId": {
              "type": "raster",
              "tiles": ["$tileUrl"],
              "tileSize": 256,
              "maxzoom": $maxZoom,
              "attribution": "$attribution"
            }
          },
          "layers": [
            {
              "id": "$sourceId",
              "type": "raster",
              "source": "$sourceId"
            }
          ]
        }
    """.trimIndent()

    private val sentinelStyleJson: String = """
        {
          "version": 8,
          "sources": {
            "s2cloudless": {
              "type": "raster",
              "tiles": [
                "https://tiles.maps.eox.at/wmts/1.0.0/s2cloudless-2024_3857/default/g/{z}/{y}/{x}.jpg"
              ],
              "tileSize": 256,
              "maxzoom": $MAX_SATELLITE_ZOOM,
              "attribution": "Sentinel-2 cloudless 2024 by EOX (CC BY 4.0)"
            }
          },
          "layers": [
            {
              "id": "s2cloudless",
              "type": "raster",
              "source": "s2cloudless"
            }
          ]
        }
    """.trimIndent()

    /** Границы масштабов, в которых имеет смысл сохранять область. */
    const val MIN_OFFLINE_ZOOM = 8.0
    const val MAX_OFFLINE_ZOOM = 14.0

    const val DEFAULT_ZOOM = 12.0

    /**
     * Диапазон масштабов для сохранения области.
     *
     * Рыболов запросто разглядывает водоём на масштабе крупнее нашего
     * потолка. Брать такой зум как нижнюю границу нельзя: `minZoom` окажется
     * больше `maxZoom`, а MapLibre отвечает на это `java.lang.Error` и роняет
     * приложение целиком. Поэтому нижняя граница всегда зажимается в
     * допустимые пределы, а верхняя не опускается ниже неё.
     */
    fun offlineZoomRange(currentZoom: Double): ClosedFloatingPointRange<Double> {
        val minZoom = currentZoom.coerceIn(MIN_OFFLINE_ZOOM, MAX_OFFLINE_ZOOM)
        return minZoom..maxOf(minZoom, MAX_OFFLINE_ZOOM)
    }
}
