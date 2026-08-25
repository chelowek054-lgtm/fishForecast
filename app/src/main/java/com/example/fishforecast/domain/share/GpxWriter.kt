package com.example.fishforecast.domain.share

import com.example.fishforecast.data.local.entities.FishingSpotEntity

/**
 * Точки выгружаются в GPX: его понимают навигаторы и эхолоты, так что
 * получателю не нужен FishForecast, чтобы доехать до места.
 */
object GpxWriter {

    fun write(spots: List<FishingSpotEntity>, fishNameById: Map<Int, String> = emptyMap()): String =
        buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine(
                """<gpx version="1.1" creator="FishForecast" """ +
                    """xmlns="http://www.topografix.com/GPX/1/1">"""
            )
            spots.forEach { spot ->
                appendLine("""  <wpt lat="${spot.latitude}" lon="${spot.longitude}">""")
                appendLine("    <name>${escape(spot.name)}</name>")
                val description = buildDescription(spot, fishNameById[spot.fishId])
                if (description.isNotEmpty()) {
                    appendLine("    <desc>${escape(description)}</desc>")
                }
                appendLine("  </wpt>")
            }
            append("</gpx>")
        }

    private fun buildDescription(spot: FishingSpotEntity, fishName: String?): String {
        val parts = mutableListOf<String>()
        if (fishName != null) parts += "Здесь берёт: $fishName"
        if (spot.note.isNotBlank()) parts += spot.note
        return parts.joinToString(". ")
    }

    /** Названия точек пишет пользователь, поэтому спецсимволы ломают XML. */
    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
