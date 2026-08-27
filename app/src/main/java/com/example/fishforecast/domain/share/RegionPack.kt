package com.example.fishforecast.domain.share

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Пакет района — то, чем рыболовы обмениваются вместо файла карты.
 *
 * Раньше наружу уходила база тайлов MapLibre целиком: сотни мегабайт, все
 * районы разом и ни слова о том, что рыболов про эти места знает. Норма
 * давления, глубины, точки и справочник видов оставались дома, а получатель
 * видел голый прямоугольник.
 *
 * Пакет устроен наоборот: это знание о месте, а не картинка. Тайлы в него
 * не входят — получатель докачает их сам по границам района при первой
 * сети. Файл получается в килобайтах, его можно отправить в мессенджере, и
 * он не раздаёт чужие тайлы, чего лицензии источников и не разрешают.
 *
 * Тот же документ примет и вернёт будущий сервер: схема, идентификаторы и
 * правила слияния описаны в `app/docs/RegionPack.md`.
 */
@Serializable
data class RegionPack(
    /**
     * Версия схемы. Читатель обязан её проверить: чужой пакет приходит из
     * будущего чаще, чем кажется, и молча съесть незнакомые поля хуже, чем
     * честно отказаться.
     */
    val schema: String = SCHEMA,
    /** Идентификатор самой посылки — не района, а именно этой отправки. */
    val id: String,
    /** Когда собран, ISO8601. */
    val createdAt: String,
    val author: PackAuthor? = null,
    val region: PackRegion,
    val zones: List<PackZone> = emptyList(),
    val spots: List<PackSpot> = emptyList(),
    /**
     * Виды рыб этой местности. Справочник ходит вместе с районом: в разных
     * водоёмах берёт разная рыба, и чужие пороги температуры и давления —
     * такое же знание о месте, как глубина ямы.
     */
    val fish: List<PackFish> = emptyList()
) {
    companion object {
        const val SCHEMA = "fishforecast.region-pack/1"

        /** Расширение файла: по нему система предлагает открыть приложением. */
        const val FILE_EXTENSION = "ffpack"
        const val MIME_TYPE = "application/json"
    }
}

/**
 * Кто собрал пакет. Имя необязательное и вводится самим рыболовом: в общей
 * базе важно видеть, чей это район, но требовать учётную запись ради обмена
 * файлом незачем.
 */
@Serializable
data class PackAuthor(
    val id: String,
    val name: String? = null
)

@Serializable
data class PackRegion(
    /** Глобальный идентификатор района: он же ключ в общей базе. */
    val id: String,
    val name: String,
    val bounds: PackBounds,
    val minZoom: Double,
    val maxZoom: Double,
    /**
     * Норма давления, мм рт. ст. Пересчитывать её получателю не нужно:
     * она посчитана по наблюдениям за это самое место.
     */
    @SerialName("normalPressureMmHg")
    val normalPressureMmHg: Double? = null,
    val elevationM: Double? = null,
    /** Глубины мели и ямы, м — без них не считается температура воды. */
    val shallowDepthM: Double? = null,
    val deepDepthM: Double? = null
)

@Serializable
data class PackBounds(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
)

/** Обведённая рыболовом граница вместе со своими секторами. */
@Serializable
data class PackZone(
    val id: String,
    val name: String,
    /** `WATER` или `SHORE`. */
    val kind: String,
    val outline: List<PackPoint>,
    val note: String = "",
    val sectors: List<PackSector> = emptyList()
)

@Serializable
data class PackSector(
    val id: String,
    val name: String,
    val outline: List<PackPoint>,
    val note: String = ""
)

@Serializable
data class PackPoint(
    val lat: Double,
    val lon: Double
)

@Serializable
data class PackSpot(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    /** `SHORE` или `WATER`: где встать против того, куда бросать. */
    val placement: String,
    val note: String = "",
    /** Вид рыбы по его глобальному идентификатору; null — не привязана. */
    val fishId: String? = null,
    val zoneId: String? = null,
    val sectorId: String? = null
)

@Serializable
data class PackFish(
    val id: String,
    val name: String,
    val description: String = "",
    val minTempC: Double,
    val maxTempC: Double,
    val minPressureMmHg: Double,
    val maxPressureMmHg: Double
)
