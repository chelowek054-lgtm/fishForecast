package com.example.fishforecast.domain.fish

import com.example.fishforecast.data.local.entities.FishEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Справочник видов: формат файла в ассетах и того же документа на сервере.
 *
 * Формат отделён от таблицы Room намеренно. Справочник — общий язык: он
 * лежит в ассетах, приезжает с сервера и правится руками в редакторе.
 * Внутренние поля базы меняются вместе с приложением, а этот документ
 * должен читаться и старыми, и новыми сборками.
 */
@Serializable
data class FishCatalog(
    val schema: String = SCHEMA,
    /** Растёт при каждом обновлении: по нему видно, есть ли что качать. */
    val version: Int = 0,
    val updatedAt: String? = null,
    val fish: List<CatalogFish> = emptyList()
) {
    companion object {
        const val SCHEMA = "fishforecast.fish-catalog/1"
    }
}

@Serializable
data class CatalogFish(
    /** Осмысленный слаг: `carp`, `pike`. Он же ключ обновления. */
    val id: String,
    val name: String,
    val description: String = "",
    val temp: CatalogTemp,
    val pressure: CatalogPressure,
    val oxygen: CatalogOxygen = CatalogOxygen(),
    @SerialName("default_horizon")
    val defaultHorizon: String = FishEntity.HORIZON_BOTTOM,
    @SerialName("cold_temp_threshold")
    val coldTempThreshold: Double = 12.0,
    /** `predator` или `peaceful`. */
    val guild: String = "peaceful",
    /**
     * Активность по фазам света: `night`, `dawn`, `morning`, `day`,
     * `evening`, `dusk`. Пусто — берётся профиль гильдии.
     */
    @SerialName("light_activity")
    val lightActivity: Map<String, Double> = emptyMap(),
    /** Структуры из словаря знаний, где вид держится. */
    @SerialName("preferred_structures")
    val preferredStructures: List<String> = emptyList(),
    val baits: CatalogBaits = CatalogBaits(),
    @SerialName("groundbait_rules")
    val groundbaitRules: CatalogGroundbaitRules = CatalogGroundbaitRules()
)

@Serializable
data class CatalogTemp(
    @SerialName("opt_min") val optMin: Double,
    @SerialName("opt_max") val optMax: Double,
    @SerialName("abs_min") val absMin: Double,
    @SerialName("abs_max") val absMax: Double
)

/**
 * Что вид терпит по давлению.
 *
 * Отсчёт идёт от нормы конкретного водоёма, а не от абсолютных
 * миллиметров: на высоте 500 м нормальные для места 710 мм по абсолютной
 * шкале выглядят катастрофой, хотя рыба там живёт всю жизнь.
 *
 * Допуск несимметричен намеренно: падение рыба переносит легче роста —
 * падающее давление она встречает кормлением, растущее вгоняет её в апатию.
 *
 * [minMmHg] и [maxMmHg] остались от прежней, абсолютной записи. Расчёт их не
 * читает; они лежат, пока справочник живёт в поколении `/1`, чтобы старые
 * сборки могли разобрать новый документ.
 */
@Serializable
data class CatalogPressure(
    @SerialName("max_drop_mmhg") val maxDropMmHg: Double = DEFAULT_TOLERANCE,
    @SerialName("max_rise_mmhg") val maxRiseMmHg: Double = DEFAULT_TOLERANCE,
    @SerialName("min_mmHg") val minMmHg: Double? = null,
    @SerialName("max_mmHg") val maxMmHg: Double? = null
) {
    companion object {
        /** Столько терпит рыба, про которую ничего не сказано. */
        const val DEFAULT_TOLERANCE = 12.0
    }
}

@Serializable
data class CatalogOxygen(
    @SerialName("comfort_mg_l") val comfortMgL: Double = 5.0,
    @SerialName("critical_mg_l") val criticalMgL: Double = 3.0
)

@Serializable
data class CatalogBaits(
    val cold: List<String> = emptyList(),
    val warm: List<String> = emptyList()
)

@Serializable
data class CatalogGroundbaitRules(
    val cold: GroundbaitRule = GroundbaitRule(),
    val warm: GroundbaitRule = GroundbaitRule()
)

/**
 * Как кормить. Значения оставлены словарными (`low`, `fine`, `sweet_fruity`),
 * а не переведёнными: перевод — дело экрана, а справочник ходит между
 * устройствами и сервером.
 */
@Serializable
data class GroundbaitRule(
    val volume: String = "none",
    val fraction: String = "none",
    val sweetness: String = "none",
    @SerialName("flavor_profile")
    val flavorProfile: String = "none",
    val notes: String = ""
)

object FishCatalogCodec {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Читает справочник.
     *
     * Документ бывает двух видов: обёрнутый в объект с версией — так его
     * отдаёт сервер — и голым массивом видов, как удобно править руками.
     * Второй случай поддержан намеренно: справочник в ассетах должен
     * оставаться пригодным для правки в текстовом редакторе.
     */
    fun decode(text: String): Result<FishCatalog> = runCatching {
        val element = json.parseToJsonElement(text)
        if (element is JsonArray) {
            FishCatalog(fish = json.decodeFromJsonElement(FishListSerializer, element))
        } else {
            val catalog = json.decodeFromJsonElement(FishCatalog.serializer(), element)
            require(catalog.schema.substringBeforeLast('/') == FishCatalog.SCHEMA.substringBeforeLast('/')) {
                "Это не справочник рыб FishForecast"
            }
            require(
                catalog.schema.substringAfterLast('/') == FishCatalog.SCHEMA.substringAfterLast('/')
            ) {
                "Справочник новее приложения (схема ${catalog.schema})"
            }
            catalog
        }.also { catalog ->
            require(catalog.fish.isNotEmpty()) { "В справочнике нет ни одного вида" }
            require(catalog.fish.all { it.id.isNotBlank() }) { "У вида нет идентификатора" }
        }
    }

    private val FishListSerializer = ListSerializer(CatalogFish.serializer())
}

/** Перевод вида справочника в строку базы. */
fun CatalogFish.toEntity(existing: FishEntity? = null): FishEntity = FishEntity(
    id = existing?.id ?: 0,
    uid = id,
    name = name,
    // Описание рыболов правит под себя — чужое не затирает своё.
    description = existing?.description?.takeIf { it.isNotBlank() } ?: description,
    optMinTemp = temp.optMin.toFloat(),
    optMaxTemp = temp.optMax.toFloat(),
    absMinTemp = temp.absMin.toFloat(),
    absMaxTemp = temp.absMax.toFloat(),
    maxPressureDrop = pressure.maxDropMmHg.toFloat(),
    maxPressureRise = pressure.maxRiseMmHg.toFloat(),
    oxygenComfortMgL = oxygen.comfortMgL.toFloat(),
    oxygenCriticalMgL = oxygen.criticalMgL.toFloat(),
    defaultHorizon = defaultHorizon,
    coldTempThreshold = coldTempThreshold.toFloat(),
    guild = guild,
    lightActivity = lightActivity.encodeLightActivity(),
    preferredStructures = preferredStructures.encodeBaits(),
    baitsCold = baits.cold.encodeBaits(),
    baitsWarm = baits.warm.encodeBaits(),
    groundbaitCold = FishCatalogCodec.json.encodeToString(
        GroundbaitRule.serializer(),
        groundbaitRules.cold
    ),
    groundbaitWarm = FishCatalogCodec.json.encodeToString(
        GroundbaitRule.serializer(),
        groundbaitRules.warm
    ),
    imageUrl = existing?.imageUrl
)

private val BaitsSerializer = ListSerializer(String.serializer())

private val LightSerializer = MapSerializer(String.serializer(), Double.serializer())

fun Map<String, Double>.encodeLightActivity(): String =
    FishCatalogCodec.json.encodeToString(LightSerializer, this)

/** Профиль света из строки базы; пусто — значит, берётся профиль гильдии. */
fun String.decodeLightActivity(): Map<String, Double> = runCatching {
    FishCatalogCodec.json.decodeFromString(LightSerializer, this)
}.getOrDefault(emptyMap())

fun List<String>.encodeBaits(): String =
    FishCatalogCodec.json.encodeToString(BaitsSerializer, this)

/** Наживки из строки базы; пустой список, если запись повреждена. */
fun String.decodeBaits(): List<String> = runCatching {
    FishCatalogCodec.json.decodeFromString(BaitsSerializer, this)
}.getOrDefault(emptyList())

fun String.decodeGroundbait(): GroundbaitRule = runCatching {
    FishCatalogCodec.json.decodeFromString(GroundbaitRule.serializer(), this)
}.getOrDefault(GroundbaitRule())

/**
 * Обратный перевод: строка базы в вид справочника.
 *
 * Нужен, когда район уезжает к другому рыболову: виды едут вместе с ним в
 * том же формате, в каком приходят с сервера, — двух описаний одного и того
 * же не бывает.
 */
fun FishEntity.toCatalogFish(): CatalogFish = CatalogFish(
    id = uid,
    name = name,
    description = description,
    temp = CatalogTemp(
        optMin = optMinTemp.toDouble(),
        optMax = optMaxTemp.toDouble(),
        absMin = absMinTemp.toDouble(),
        absMax = absMaxTemp.toDouble()
    ),
    pressure = CatalogPressure(
        maxDropMmHg = maxPressureDrop.toDouble(),
        maxRiseMmHg = maxPressureRise.toDouble()
    ),
    oxygen = CatalogOxygen(
        comfortMgL = oxygenComfortMgL.toDouble(),
        criticalMgL = oxygenCriticalMgL.toDouble()
    ),
    defaultHorizon = defaultHorizon,
    coldTempThreshold = coldTempThreshold.toDouble(),
    guild = guild,
    lightActivity = lightActivity.decodeLightActivity(),
    preferredStructures = preferredStructures.decodeBaits(),
    baits = CatalogBaits(cold = baitsCold.decodeBaits(), warm = baitsWarm.decodeBaits()),
    groundbaitRules = CatalogGroundbaitRules(
        cold = groundbaitCold.decodeGroundbait(),
        warm = groundbaitWarm.decodeGroundbait()
    )
)
