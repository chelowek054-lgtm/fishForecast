package com.example.fishforecast.domain.share

import com.example.fishforecast.data.local.entities.FishingSpotEntity
import org.w3c.dom.Element
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Чтение точек из GPX. Файл приходит извне — из навигатора, эхолота или от
 * другого рыболова, — поэтому парсер не полагается на порядок и полноту
 * полей: без координат точка пропускается, всё остальное необязательно.
 */
object GpxParser {

    private val DEFENSIVE_FEATURES = listOf(
        "http://apache.org/xml/features/disallow-doctype-decl" to true,
        "http://xml.org/sax/features/external-general-entities" to false,
        "http://xml.org/sax/features/external-parameter-entities" to false
    )

    fun parse(input: InputStream): List<FishingSpotEntity> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            // Файл чужой: внешние сущности могли бы утащить локальные файлы.
            // Парсер Android не знает части этих настроек, поэтому каждая
            // применяется отдельно и не роняет импорт, если не поддержана.
            DEFENSIVE_FEATURES.forEach { (feature, enabled) ->
                runCatching { setFeature(feature, enabled) }
            }
            isExpandEntityReferences = false
        }

        val document = factory.newDocumentBuilder().parse(input)
        val waypoints = document.getElementsByTagName("wpt")

        return (0 until waypoints.length).mapNotNull { index ->
            (waypoints.item(index) as? Element)?.toSpot()
        }
    }

    private fun Element.toSpot(): FishingSpotEntity? {
        val latitude = getAttribute("lat").toDoubleOrNull() ?: return null
        val longitude = getAttribute("lon").toDoubleOrNull() ?: return null

        return FishingSpotEntity(
            name = childText("name").ifBlank { "Импортированная точка" },
            latitude = latitude,
            longitude = longitude,
            note = childText("desc")
        )
    }

    private fun Element.childText(tag: String): String {
        val nodes = getElementsByTagName(tag)
        if (nodes.length == 0) return ""
        return nodes.item(0).textContent?.trim().orEmpty()
    }
}
