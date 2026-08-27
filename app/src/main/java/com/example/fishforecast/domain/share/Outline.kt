package com.example.fishforecast.domain.share

/**
 * Контур зоны в базе лежит строкой «широта,долгота;широта,долгота».
 *
 * Своя таблица вершин была бы честнее по нормализации, но контур всегда
 * читается и пишется целиком, а его размер — десятки точек. Строка проще
 * и переживает обмен пакетами без отдельной сущности.
 */
data class GeoPoint(val latitude: Double, val longitude: Double)

fun List<GeoPoint>.encodeOutline(): String =
    joinToString(";") { "%.6f,%.6f".format(java.util.Locale.US, it.latitude, it.longitude) }

fun String.decodeOutline(): List<GeoPoint> =
    split(';')
        .mapNotNull { pair ->
            val parts = pair.split(',')
            if (parts.size != 2) return@mapNotNull null
            val lat = parts[0].trim().toDoubleOrNull() ?: return@mapNotNull null
            val lon = parts[1].trim().toDoubleOrNull() ?: return@mapNotNull null
            GeoPoint(lat, lon)
        }

/** Замкнутый контур: меньше трёх вершин площади не задают. */
fun List<GeoPoint>.isPolygon(): Boolean = size >= 3
