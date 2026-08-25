package com.example.fishforecast.ui.map

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

    /** Границы масштабов, в которых имеет смысл сохранять область. */
    const val MIN_OFFLINE_ZOOM = 8.0
    const val MAX_OFFLINE_ZOOM = 14.0

    const val DEFAULT_ZOOM = 12.0
}
