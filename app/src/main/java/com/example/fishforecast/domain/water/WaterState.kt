package com.example.fishforecast.domain.water

import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.knowledge.WaterBodyType
import com.example.fishforecast.domain.weather.kmhToMs
import java.time.LocalDateTime

/**
 * Ход воды в двух слоях района.
 *
 * Две кривые вместо одной — весь смысл расчёта: разница между мелью и ямой
 * и говорит, где сегодня рыба. Одна усреднённая температура водоёма такой
 * подсказки не даёт.
 */
data class WaterState(
    val shallow: List<WaterHour>,
    val deep: List<WaterHour>,
    val shallowDepthM: Double,
    val deepDepthM: Double,
    /** Глубины не заданы рыболовом — значит, взяты типовые. */
    val depthsAssumed: Boolean,
    /** Расчёт опирается на замер термометром, а не только на погоду. */
    val anchored: Boolean,
    /**
     * Кислород на мели по часам, мг/л. Считается здесь, а не в расчёте
     * клёва: он свойство воды, а не рыбы, и одинаков для всех видов.
     */
    val oxygen: Map<String, Double> = emptyMap(),
    /**
     * Кислород в яме по часам, мг/л.
     *
     * Отдельно от мели, потому что после расслоения это разная вода: наверху
     * ветер и водоросли, внизу термоклин и донный расход. Брать для ямы
     * кислород мели значило завышать её шансы ровно там, где рыбе нечем
     * дышать.
     */
    val oxygenDeep: Map<String, Double> = emptyMap(),
    /** Тип водоёма, по которому всё это посчитано; null — считали прудом. */
    val waterBody: WaterBodyType? = null
) {
    val isEmpty: Boolean get() = shallow.isEmpty()

    /** Слой на заданный час; null, пока расчёт не дошёл до этого времени. */
    fun shallowAt(time: String): Double? = shallow.firstOrNull { it.time == time }?.temperature

    fun deepAt(time: String): Double? = deep.firstOrNull { it.time == time }?.temperature

    /** Кислород на мели: столбец, который видно с берега. */
    fun oxygenAt(time: String): Double? = oxygen[time]

    /** Кислород выбранного слоя. */
    fun oxygenAt(time: String, layer: WaterLayerChoice): Double? = when (layer) {
        WaterLayerChoice.SHALLOW -> oxygen[time]
        WaterLayerChoice.DEEP -> oxygenDeep[time] ?: oxygen[time]
    }

    /** Температура выбранного слоя: мель или яма. */
    fun layerAt(time: String, layer: WaterLayerChoice): Double? = when (layer) {
        WaterLayerChoice.SHALLOW -> shallowAt(time)
        WaterLayerChoice.DEEP -> deepAt(time)
    }
}

/**
 * Считает воду для карты. Глубины берёт у рыболова, а если он их не задал —
 * типовые для небольшого водоёма, и честно об этом сообщает.
 */
fun calculateWaterState(
    forecast: List<WeatherEntity>,
    map: SavedMapEntity?,
    waterBody: WaterBodyType? = null
): WaterState {
    val shallowLayer = map?.shallowDepthM?.let { WaterLayer(it) } ?: DEFAULT_SHALLOW
    val deepLayer = map?.deepDepthM?.let { WaterLayer(it) } ?: DEFAULT_DEEP

    val anchor = map?.let { saved ->
        val temperature = saved.waterTempC
        val time = saved.waterTempAt
        if (temperature != null && time != null) WaterMeasurement(time, temperature) else null
    }

    val shallow = simulateWaterTemperature(forecast, shallowLayer, anchor, waterBody)
    val deepHours = simulateWaterTemperature(forecast, deepLayer, anchor, waterBody)
    val sorted = forecast.sortedBy { it.time }

    return WaterState(
        shallow = shallow,
        deep = deepHours,
        shallowDepthM = shallowLayer.depthM,
        deepDepthM = deepLayer.depthM,
        depthsAssumed = map?.shallowDepthM == null || map.deepDepthM == null,
        anchored = anchor != null,
        oxygen = shallow.associate { hour ->
            val index = sorted.indexOfFirst { it.time == hour.time }
            hour.time to availableOxygenMgL(
                waterTemperatureC = hour.temperature,
                waterBody = waterBody,
                darkHours = sorted.darkHoursBefore(index),
                windMs = sorted.getOrNull(index)?.windSpeed?.kmhToMs() ?: 0.0
            )
        },
        oxygenDeep = deepLayerOxygen(shallow, deepHours, sorted, waterBody),
        waterBody = waterBody
    )
}

/**
 * Кислород в яме по часам.
 *
 * Расслоение накапливается: чем дольше слои разделены, тем больше яма
 * потратила и тем меньше в ней осталось. Поэтому считается не мгновенная
 * разница, а сколько часов подряд она держится.
 */
private fun deepLayerOxygen(
    shallow: List<WaterHour>,
    deep: List<WaterHour>,
    sorted: List<WeatherEntity>,
    waterBody: WaterBodyType?
): Map<String, Double> {
    val shallowByTime = shallow.associate { it.time to it.temperature }
    var stratifiedHours = 0

    return deep.associate { hour ->
        val above = shallowByTime[hour.time]
        stratifiedHours = if (above != null && isStratified(above, hour.temperature)) {
            stratifiedHours + 1
        } else {
            0
        }

        val index = sorted.indexOfFirst { it.time == hour.time }
        hour.time to deepOxygenMgL(
            deepTemperatureC = hour.temperature,
            shallowTemperatureC = above ?: hour.temperature,
            waterBody = waterBody,
            stratifiedHours = stratifiedHours,
            darkHours = sorted.darkHoursBefore(index),
            windMs = sorted.getOrNull(index)?.windSpeed?.kmhToMs() ?: 0.0
        )
    }
}

/**
 * Сколько часов подряд перед этим не было солнца.
 *
 * Ночь определяется приходом радиации, а не часами: летом она короткая, и
 * кислород проседает меньше, чем в декабре. Заодно это работает без данных
 * о восходе — они появились позже самой модели.
 */
private fun List<WeatherEntity>.darkHoursBefore(index: Int): Int {
    if (index <= 0) return 0
    var dark = 0
    var cursor = index - 1
    while (cursor >= 0 && this[cursor].shortwaveRadiation <= DARK_RADIATION) {
        dark++
        cursor--
    }
    return dark
}

/**
 * Вт/м², ниже которых считаем, что солнца нет.
 *
 * Граница света нужна и расчёту клёва: ход воды до рассвета и после него —
 * это два разных хода, и складывать их в одно число нельзя.
 */
const val DARK_RADIATION = 1.0

/**
 * Куда идёт вода за ближайшие часы.
 *
 * Окно обрывается на границе света: до неё и после неё вода ведёт себя
 * противоположно, и шесть часов подряд через рассвет складывали ночное
 * остывание с утренним прогревом в одно число. Получалось «мель
 * прогревается» сказанное в три часа ночи — правда про сумму, ложь про
 * сейчас. Поэтому берутся только часы, у которых свет тот же, что и в
 * первом часе окна.
 */
data class WaterTrend(
    /** Насколько изменится температура за окно, °C. */
    val deltaC: Double,
    /** Сколько часов реально уместилось до границы света. */
    val hours: Int,
    /** Солнца в эти часы нет. */
    val dark: Boolean
)

fun waterTrend(
    water: List<WaterHour>,
    weather: List<WeatherEntity>,
    maxHours: Int = 6
): WaterTrend? {
    if (water.size < 2) return null
    val lit = weather.associate { it.time to (it.shortwaveRadiation > DARK_RADIATION) }

    val first = water.first()
    val litNow = lit[first.time] ?: return null
    val window = water.take(maxHours + 1).takeWhile { lit[it.time] == litNow }
    if (window.size < 2) return null

    return WaterTrend(
        deltaC = window.last().temperature - first.temperature,
        hours = window.size - 1,
        dark = !litNow
    )
}

/** Часы, оставшиеся от текущего момента и дальше. */
fun List<WaterHour>.fromNow(now: LocalDateTime = LocalDateTime.now()): List<WaterHour> =
    dropWhile { LocalDateTime.parse(it.time).isBefore(now.minusHours(1)) }
