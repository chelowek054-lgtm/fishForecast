package com.example.fishforecast.domain.water

import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.knowledge.WaterBodyType
import com.example.fishforecast.domain.weather.kmhToMs
import java.time.LocalDateTime
import kotlin.math.ln
import kotlin.math.pow

/**
 * Температура воды, посчитанная по погоде.
 *
 * Готового источника для пруда не существует: спутниковые наборы покрывают
 * море и крупные озёра, а датчика в воде у приложения нет. Зато воду можно
 * посчитать — она ведёт себя предсказуемо, просто медленно.
 *
 * Модель равновесной температуры (Эдингер): вода стремится к температуре,
 * при которой приход тепла равен расходу, но доходит до неё не сразу.
 * Скорость задаёт инерция слоя: чем глубже, тем дольше. Отсюда и главный
 * вывод, ради которого всё считается — мелководье за ночь остывает и
 * насыщается кислородом, а яма у дамбы не успевает.
 *
 * Все формулы — в единицах СИ, температура в градусах Цельсия.
 */
data class WaterHour(
    val time: String,
    /** Температура слоя, °C. */
    val temperature: Double
)

/**
 * Слой воды: рыболов задаёт свои глубины, потому что батиметрии пруда нет
 * ни в одном открытом источнике, а свой водоём он знает.
 */
data class WaterLayer(val depthM: Double) {
    init {
        require(depthM > 0) { "Глубина слоя должна быть больше нуля" }
    }
}

/** Глубины по умолчанию, пока рыболов не указал свои. */
val DEFAULT_SHALLOW = WaterLayer(1.5)
val DEFAULT_DEEP = WaterLayer(4.0)

/**
 * Прогоняет почасовую погоду через модель и возвращает ход температуры слоя.
 *
 * @param hours часы по возрастанию времени; чем больше прошедших суток, тем
 *        точнее результат — начальная ошибка затухает примерно за три
 *        постоянных времени.
 * @param anchor замер термометром: время и температура. Если он попадает в
 *        окно расчёта, модель переставляется на факт — измеренное всегда
 *        главнее посчитанного.
 */
fun simulateWaterTemperature(
    hours: List<WeatherEntity>,
    layer: WaterLayer,
    anchor: WaterMeasurement? = null,
    waterBody: WaterBodyType? = null
): List<WaterHour> {
    if (hours.isEmpty()) return emptyList()

    val sorted = hours.sortedBy { it.time }

    // Стартовать с температуры воздуха нельзя: ночью она ниже воды, днём
    // выше. Средняя за первые сутки ближе всего к тому, что в воде.
    var water = sorted.take(HOURS_PER_DAY).map { it.temperature }.average()

    val anchorTime = anchor?.let { LocalDateTime.parse(it.time) }

    return sorted.map { hour ->
        val time = LocalDateTime.parse(hour.time)
        if (anchorTime != null && anchor != null && !time.isBefore(anchorTime) &&
            time.isBefore(anchorTime.plusHours(1))
        ) {
            water = anchor.temperature
        }

        water = stepHour(water, hour, layer, waterBody)
        WaterHour(time = hour.time, temperature = water)
    }
}

/** Замер воды термометром: факт, которым модель переставляется на место. */
data class WaterMeasurement(val time: String, val temperature: Double)

/** Один час модели. */
private fun stepHour(
    water: Double,
    hour: WeatherEntity,
    layer: WaterLayer,
    waterBody: WaterBodyType?
): Double {
    val dewPoint = dewPoint(hour.temperature, hour.humidity)
    val windMs = hour.windSpeed.kmhToMs()

    // Ветер перемешивает воду тем сильнее, чем длиннее его разгон: на
    // большом озере волна работает всерьёз, в пруду её негде разогнать.
    val exchange = heatExchangeCoefficient(
        water = water,
        dewPoint = dewPoint,
        windMs = windMs * (waterBody?.windMixing ?: 1.0)
    )
    // Часть солнца отражается поверхностью, в воду уходит около 94 %.
    val absorbedSolar = hour.shortwaveRadiation * (1 - ALBEDO)
    val equilibrium = dewPoint + absorbedSolar / exchange

    // Постоянная времени слоя: ρ·c·h / K. Полтора метра отзываются за пару
    // суток, пять — почти за неделю. Тип водоёма растягивает это время:
    // реку сверху подпитывает своя вода, и суточный ход в ней сглажен.
    val inertia = waterBody?.thermalInertia?.takeIf { it > 0 } ?: 1.0
    val timeConstantSeconds = WATER_HEAT_CAPACITY * layer.depthM * inertia / exchange
    val relaxation = 1 - kotlin.math.exp(-SECONDS_PER_HOUR / timeConstantSeconds)

    return water + (equilibrium - water) * relaxation
}

/**
 * Коэффициент теплообмена поверхности, Вт/(м²·К).
 *
 * Складывается из излучения, испарения и конвекции. Ветер входит квадратом:
 * он и уносит пар, и перемешивает — поэтому в ветреную ночь вода остывает
 * заметно быстрее, чем в тихую.
 */
private fun heatExchangeCoefficient(
    water: Double,
    dewPoint: Double,
    windMs: Double
): Double {
    val windFunction = 9.2 + 0.46 * windMs.pow(2)
    val meanTemperature = (water + dewPoint) / 2
    val beta = 0.35 + 0.015 * meanTemperature + 0.0012 * meanTemperature.pow(2)
    return (4.5 + 0.05 * water + (beta + 0.47) * windFunction)
        .coerceAtLeast(MIN_EXCHANGE)
}

/**
 * Точка росы по формуле Магнуса. Она, а не температура воздуха, задаёт
 * уровень, к которому стремится вода: испарение — главный расход тепла с
 * поверхности, а оно определяется влажностью.
 */
fun dewPoint(temperature: Double, humidity: Double): Double {
    val rh = humidity.coerceIn(1.0, 100.0) / 100.0
    val gamma = ln(rh) + MAGNUS_B * temperature / (MAGNUS_C + temperature)
    return MAGNUS_C * gamma / (MAGNUS_B - gamma)
}

private const val ALBEDO = 0.06

/** ρ·c для воды: 1000 кг/м³ × 4186 Дж/(кг·К). */
private const val WATER_HEAT_CAPACITY = 4.186e6

private const val SECONDS_PER_HOUR = 3600.0
private const val HOURS_PER_DAY = 24

/** Ниже этого теплообмен не опускается даже в полный штиль. */
private const val MIN_EXCHANGE = 5.0

private const val MAGNUS_B = 17.625
private const val MAGNUS_C = 243.04
