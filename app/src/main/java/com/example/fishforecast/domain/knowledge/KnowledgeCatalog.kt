package com.example.fishforecast.domain.knowledge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Словари знаний: типы водоёмов, структуры и наблюдения на воде.
 *
 * Раньше такие вещи жили бы константами в расчёте. Но это не код, а знание:
 * оно уточняется по мере того, как рыболовы разбираются в своих водоёмах, и
 * должно обновляться отдельно от приложения — тем же способом, что и
 * справочник видов.
 *
 * Поэтому коэффициенты лежат здесь, в документе, а расчёт их читает. Формат
 * общий с сервером; неизвестные значения не роняют разбор, потому что чужой
 * справочник вправе знать больше, чем эта сборка.
 */
@Serializable
data class KnowledgeCatalog(
    val schema: String = SCHEMA,
    val version: Int = 0,
    val updatedAt: String? = null,
    val waterbodies: List<WaterBodyType> = emptyList(),
    val structures: List<StructureType> = emptyList(),
    val observations: List<ObservationType> = emptyList()
) {
    fun waterBody(id: String?): WaterBodyType? = waterbodies.firstOrNull { it.id == id }

    fun structure(id: String): StructureType? = structures.firstOrNull { it.id == id }

    fun observation(id: String): ObservationType? = observations.firstOrNull { it.id == id }

    companion object {
        const val SCHEMA = "fishforecast.knowledge/1"

        /** Пока рыболов не выбрал тип водоёма, считаем его прудом. */
        const val DEFAULT_WATERBODY = "still_small"
    }
}

/**
 * Тип водоёма: течение и размер.
 *
 * Оба свойства меняют физику, а не настроение рыбы. Течение аэрирует воду и
 * подпитывает её сверху; размер задаёт инертность и то, сумеет ли ветер
 * разогнать волну. Малый стоячий пруд перегревается за день и задыхается к
 * рассвету, река не делает ни того, ни другого.
 */
@Serializable
data class WaterBodyType(
    val id: String,
    val name: String,
    /** `still` или `flowing`. */
    val flow: String = "still",
    /** `small` или `large`. */
    val size: String = "small",
    /** Множитель инерции слоя воды: больше единицы — медленнее отзывается. */
    @SerialName("thermal_inertia")
    val thermalInertia: Double = 1.0,
    /** Доля насыщения, к которой вода реально приближается. */
    val aeration: Double = 1.0,
    /** Насколько кислорода меньше к рассвету, мг/л. */
    @SerialName("night_oxygen_drop_mg_l")
    val nightOxygenDropMgL: Double = 0.0,
    /** Насколько сильно ветер перемешивает воду: зависит от разгона волны. */
    @SerialName("wind_mixing")
    val windMixing: Double = 1.0,
    /**
     * Описано ли поведение. У моря и океана значение есть, а коэффициентов
     * нет — приложение должно сказать об этом прямо, а не считать их прудом.
     */
    @SerialName("behavior_defined")
    val behaviorDefined: Boolean = true,
    val notes: String = ""
) {
    val flowing: Boolean get() = flow == "flowing"
    val large: Boolean get() = size == "large"
}

/**
 * Структура берега или дна: то, что делает место местом.
 *
 * Каждая даёт что-то своё — укрытие, корм, тень, кислород, — и потому
 * по-разному важна хищнику и мирной рыбе: коряжник это засада, заводь это
 * столовая.
 */
@Serializable
data class StructureType(
    val id: String,
    val name: String,
    /** Что структура даёт: `shelter`, `food`, `shade`, `oxygen`, `depth`… */
    val gives: List<String> = emptyList(),
    @SerialName("predator_bonus")
    val predatorBonus: Double = 0.0,
    @SerialName("peaceful_bonus")
    val peacefulBonus: Double = 0.0,
    /** Поправка к кислороду, мг/л: приток добавляет, гнилой ил отнимает. */
    @SerialName("oxygen_bonus_mg_l")
    val oxygenBonusMgL: Double = 0.0,
    /** Поправка к температуре воды, °C: донный ключ холодит место. */
    @SerialName("water_offset_c")
    val waterOffsetC: Double = 0.0,
    val notes: String = ""
)

/**
 * Наблюдение с берега: всплеск малька, птицы, плёнка на воде.
 *
 * Это факт, а не прогноз, и живёт он недолго — потому у каждого свой срок.
 * В общую оценку такие вещи не подмешиваются молча: рыболов должен видеть,
 * что подсказка изменилась именно из-за того, что он сам заметил.
 */
@Serializable
data class ObservationType(
    val id: String,
    val name: String,
    /** Кого касается: `predator`, `peaceful` или `any`. */
    val guild: String = "any",
    /** Сдвиг оценки места, доля: со знаком. */
    val effect: Double = 0.0,
    /** Сколько часов наблюдение имеет силу. */
    val hours: Int = 3,
    val notes: String = ""
)

object KnowledgeCodec {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Читает словари и проверяет схему.
     *
     * Поколение схемы должно совпадать: словарь из будущей версии может
     * описывать коэффициенты, которых эта сборка не понимает, и молча взять
     * половину — хуже, чем отказаться.
     */
    fun decode(text: String): Result<KnowledgeCatalog> = runCatching {
        val catalog = json.decodeFromString(KnowledgeCatalog.serializer(), text)
        require(
            catalog.schema.substringBeforeLast('/') ==
                KnowledgeCatalog.SCHEMA.substringBeforeLast('/')
        ) {
            "Это не словари знаний FishForecast"
        }
        require(
            catalog.schema.substringAfterLast('/') ==
                KnowledgeCatalog.SCHEMA.substringAfterLast('/')
        ) {
            "Словари новее приложения (схема ${catalog.schema})"
        }
        require(catalog.waterbodies.isNotEmpty()) { "В словаре нет ни одного типа водоёма" }
        catalog
    }

    fun encode(catalog: KnowledgeCatalog): String =
        json.encodeToString(KnowledgeCatalog.serializer(), catalog)
}
