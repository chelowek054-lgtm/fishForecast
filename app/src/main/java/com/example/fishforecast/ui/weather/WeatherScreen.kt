package com.example.fishforecast.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.DailySunEntity
import com.example.fishforecast.data.local.entities.PressureLogEntity
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.domain.water.WaterState
import com.example.fishforecast.domain.water.fromNow
import com.example.fishforecast.domain.water.oxygenLevel
import com.example.fishforecast.domain.water.oxygenLevelText
import com.example.fishforecast.domain.water.oxygenSaturationMgL
import com.example.fishforecast.domain.water.waterTrend
import com.example.fishforecast.domain.weather.DailyForecast
import com.example.fishforecast.domain.weather.PressureDirection
import com.example.fishforecast.domain.weather.HourWindow
import com.example.fishforecast.domain.weather.hourWindow
import com.example.fishforecast.domain.weather.kmhToMs
import com.example.fishforecast.domain.weather.pressureTrend
import com.example.fishforecast.domain.weather.skyOf
import com.example.fishforecast.domain.weather.toDailyForecast
import com.example.fishforecast.domain.weather.weatherCodeText
import com.example.fishforecast.domain.weather.windArrowRotation
import com.example.fishforecast.domain.weather.windDescription
import com.example.fishforecast.domain.weather.windDirectionLabel
import com.example.fishforecast.ui.common.NoActiveMapMessage
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** Сколько часов показывать вперёд на графиках ближайшего времени. */
private const val HOURS_FORWARD = 24

/**
 * И сколько назад. Прогноз без прошлого отвечает только «что будет», а
 * рыболову нужно «что происходит»: падение давления читается в сравнении.
 */
private const val HOURS_BACK = 12

/** Насколько бледнее показываются прошедшие часы. */
private const val PAST_ALPHA = 0.45f

/** Ход воды меньше этого за окно — считаем, что вода стоит. */
private const val COOLING_THRESHOLD = 0.5

/** Насколько порыв должен превысить ветер, чтобы о нём стоило говорить. */
private const val GUST_MARGIN_KMH = 8.0

/** Окно, по которому оценивается движение давления. */
private const val TREND_WINDOW_HOURS = 6

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onOpenMap: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val forecast by viewModel.forecast.collectAsState()
    val activeMap by viewModel.activeMap.collectAsState()
    val localPressure by viewModel.localPressure.collectAsState()
    val sunTimes by viewModel.sunTimes.collectAsState()
    val water by viewModel.water.collectAsState()
    val pressureLog by viewModel.pressureLog.collectAsState()
    val normalPressure by viewModel.normalPressure.collectAsState()
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value

    LaunchedEffect(Unit) {
        if (forecast.isEmpty()) {
            viewModel.loadWeatherInfo()
        }
    }

    // В базе лежат и прошедшие часы этих суток: Open-Meteo отдаёт сутки
    // целиком. Отсчёт ведём от ближайшего к текущему времени часа, иначе
    // «сейчас» показывало бы полночь.
    val currentIndex = remember(forecast) { forecast.indexOfCurrentHour() }
    val current = forecast.getOrNull(currentIndex)
    val window = remember(forecast) {
        hourWindow(forecast, HOURS_BACK, HOURS_FORWARD) { it.time }
    }
    val upcoming = remember(forecast, currentIndex) {
        forecast.drop(currentIndex).take(HOURS_FORWARD)
    }
    // Неделя считается от сегодняшнего дня: в кэше теперь лежат и прошедшие
    // сутки, но прогноз на вчера рыболову ни к чему.
    val days = remember(forecast) {
        val today = LocalDate.now()
        forecast.filter { !LocalDateTime.parse(it.time).toLocalDate().isBefore(today) }
            .toDailyForecast()
    }
    val yesterday = remember(forecast, currentIndex) {
        forecast.hourNearest(LocalDateTime.now().minusDays(1))
    }
    val todaySun = sunTimes.firstOrNull { it.date == LocalDate.now().toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Прогноз погоды")
                        // Рыболову важно видеть, для какого района цифры.
                        activeMap?.let { map ->
                            Text(
                                text = map.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadWeatherInfo() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeMap == null) {
                NoActiveMapMessage(
                    explanation = "Погода запрашивается по центру выбранного района.",
                    onOpenMap = onOpenMap
                )
            } else if (isLoading && forecast.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null && forecast.isEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CurrentWeatherCard(
                            weather = current,
                            yesterday = yesterday,
                            sun = todaySun,
                            normalPressureMmHg = normalPressure,
                            trend = pressureTrend(upcoming.take(TREND_WINDOW_HOURS))
                        )
                    }

                    if (!water.isEmpty && current != null) {
                        item {
                            WaterCard(water = water, currentTime = current.time)
                        }
                        if (upcoming.size >= 2) {
                            item {
                                ChartSection(
                                    title = "Температура воды",
                                    subtitle = "Мель %.1f м и яма %.1f м%s".format(
                                        water.shallowDepthM,
                                        water.deepDepthM,
                                        if (water.depthsAssumed) {
                                            " — глубины типовые, задайте свои в списке карт"
                                        } else {
                                            ""
                                        }
                                    )
                                ) {
                                    val shallow = hourWindow(
                                        items = water.shallow,
                                        hoursBack = HOURS_BACK,
                                        hoursForward = HOURS_FORWARD
                                    ) { it.time }.items
                                    val deep = hourWindow(
                                        items = water.deep,
                                        hoursBack = HOURS_BACK,
                                        hoursForward = HOURS_FORWARD
                                    ) { it.time }.items
                                    MultiLineChart(
                                        labels = shallow.map { it.time.timeOnly() },
                                        series = listOf(
                                            ChartSeries(
                                                values = shallow.map { it.temperature },
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            ChartSeries(
                                                values = deep.map { it.temperature },
                                                color = MaterialTheme.colorScheme.outline,
                                                labelAbove = false
                                            )
                                        ),
                                        valueSuffix = "°"
                                    )
                                }
                            }
                        }
                    }

                    if (upcoming.size >= 2) {
                        item {
                            ChartSection(
                                title = "Ход погоды",
                                subtitle = "$HOURS_BACK часов назад и $HOURS_FORWARD вперёд"
                            ) {
                                HourlyStrip(window)
                            }
                        }
                        item {
                            ChartSection(
                                title = "Температура",
                                subtitle = "От −$HOURS_BACK ч до +$HOURS_FORWARD ч"
                            ) {
                                LineChart(
                                    points = window.items.map { hour ->
                                        ChartPoint(
                                            label = hour.hourLabel(),
                                            value = hour.temperature
                                        )
                                    },
                                    valueSuffix = "°",
                                    // Окно стало шире: подписи через каждые
                                    // три часа налезали бы друг на друга.
                                    labelEvery = 6,
                                    highlightIndex = window.nowIndex
                                )
                            }
                        }
                        item {
                            ChartSection(
                                title = "Давление, мм рт. ст.",
                                subtitle = normalPressure?.let { normal ->
                                    "Пунктиром — норма района: %d мм, среднее по наблюдениям"
                                        .format(normal.roundToInt())
                                } ?: "Норма появится после первого обновления с сетью"
                            ) {
                                LineChart(
                                    points = window.items.map { hour ->
                                        ChartPoint(
                                            label = hour.hourLabel(),
                                            value = hour.pressure.hPaToMmHg()
                                        )
                                    },
                                    valueSuffix = "",
                                    lineColor = MaterialTheme.colorScheme.tertiary,
                                    labelEvery = 6,
                                    highlightIndex = window.nowIndex,
                                    referenceValue = normalPressure,
                                    referenceLabel = "норма"
                                )
                            }
                        }
                    }

                    if (viewModel.hasBarometer) {
                        item {
                            LocalBarometerCard(
                                localPressure = localPressure,
                                forecastPressureHpa = current?.pressure,
                                normalPressureMmHg = normalPressure,
                                dayAgoPressureHpa = pressureLog.pressureDayAgo()
                            )
                        }
                    }

                    if (days.size >= 2) {
                        item {
                            WeekForecast(days)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Текущий час: крупная температура и всё, что влияет на клёв прямо сейчас.
 */
@Composable
private fun CurrentWeatherCard(
    weather: WeatherEntity?,
    yesterday: WeatherEntity?,
    sun: DailySunEntity?,
    normalPressureMmHg: Double?,
    trend: com.example.fishforecast.domain.weather.PressureTrend?
) {
    if (weather == null) return

    val pressureMmHg = weather.pressure.hPaToMmHg()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = skyOf(weather.weatherCode).icon(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "%+d°".format(weather.temperature.roundToInt()),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weatherCodeText(weather.weatherCode),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // Похолодание после жары — момент, когда рыба оживает.
                    // Увидеть его можно только в сравнении со вчерашним днём.
                    yesterday?.let { past ->
                        Text(
                            text = "Вчера в это время %+d°, %d мм".format(
                                past.temperature.roundToInt(),
                                past.pressure.hPaToMmHg().roundToInt()
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherFact(
                    title = "Давление",
                    value = "${pressureMmHg.roundToInt()} мм",
                    detail = pressureDetail(pressureMmHg, normalPressureMmHg, trend)
                )
                WeatherFact(
                    title = "Ветер",
                    value = "%.1f м/с".format(weather.windSpeed.kmhToMs()),
                    detail = "${windDirectionLabel(weather.windDirection)}, " +
                        if (weather.windGusts > weather.windSpeed + GUST_MARGIN_KMH) {
                            "порывы до %.0f".format(weather.windGusts.kmhToMs())
                        } else {
                            windDescription(weather.windSpeed)
                        },
                    windDirection = weather.windDirection
                )
                WeatherFact(
                    title = "Осадки",
                    value = "${weather.precipitationChance.roundToInt()}%",
                    detail = if (weather.precipitation > 0) {
                        "%.1f мм за час".format(weather.precipitation)
                    } else {
                        "сухо"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherFact(
                    title = "Облачность",
                    value = "${weather.cloudCover.roundToInt()}%",
                    detail = cloudDetail(weather.cloudCover)
                )
                WeatherFact(
                    title = "Влажность",
                    value = "${weather.humidity.roundToInt()}%",
                    detail = yesterday?.let {
                        "вчера ${it.humidity.roundToInt()}%"
                    } ?: "—"
                )
                // Зори — главные окна клёва, и рассвет сдвигается быстрее,
                // чем кажется: за месяц набегает больше часа.
                WeatherFact(
                    title = "Зори",
                    value = sun?.let { it.sunrise.timeOnly() } ?: "—",
                    detail = sun?.let { "закат ${it.sunset.timeOnly()}" } ?: "нет данных"
                )
            }
        }
    }
}

/**
 * Вода и кислород — то, ради чего считается погода.
 *
 * Показаны оба слоя: разница между мелью и ямой и подсказывает, где рыба.
 * Кислород выводится из температуры, поэтому стоит рядом, а не отдельно.
 */
@Composable
private fun WaterCard(water: WaterState, currentTime: String) {
    val shallowNow = water.shallowAt(currentTime) ?: return
    val deepNow = water.deepAt(currentTime) ?: return
    val oxygenNow = water.oxygenAt(currentTime) ?: oxygenSaturationMgL(shallowNow)
    val trend = waterTrend(water.shallow.fromNow())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = buildString {
                    append(if (water.anchored) "Вода (от вашего замера)" else "Вода (расчёт по погоде)")
                    water.waterBody?.let { append(" · ${it.name}") }
                },
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WaterLayerFact(
                    title = "Мель %.1f м".format(water.shallowDepthM),
                    temperature = shallowNow
                )
                WaterLayerFact(
                    title = "Яма %.1f м".format(water.deepDepthM),
                    temperature = deepNow
                )
                WeatherFact(
                    title = "Кислород",
                    value = "%.1f".format(oxygenNow),
                    detail = oxygenLevelText(oxygenLevel(oxygenNow))
                )
            }

            trend?.let { delta ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        delta <= -COOLING_THRESHOLD ->
                            "Мель остывает на %.1f° за %d часов — вода насыщается кислородом"
                                .format(abs(delta), TREND_WINDOW_HOURS)
                        delta >= COOLING_THRESHOLD ->
                            "Мель прогревается на %.1f° за %d часов — кислорода станет меньше"
                                .format(delta, TREND_WINDOW_HOURS)
                        else -> "Вода стоит ровно"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun WaterLayerFact(title: String, temperature: Double) {
    Column(
        modifier = Modifier.width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.labelSmall)
        Text(
            text = "%.1f°".format(temperature),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "%.1f мг/л".format(oxygenSaturationMgL(temperature)),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** Облачность решает, будет ли солнце греть воду. */
private fun cloudDetail(cloudCover: Double): String = when {
    cloudCover < 20 -> "солнце греет воду"
    cloudCover < 60 -> "солнце с перерывами"
    else -> "вода не прогревается"
}

/**
 * Что происходит с давлением: цифра сама по себе рыболову ничего не
 * говорит, важны отклонение от нормы водоёма и направление движения.
 */
private fun pressureDetail(
    pressureMmHg: Double,
    normalPressureMmHg: Double?,
    trend: com.example.fishforecast.domain.weather.PressureTrend?
): String {
    val parts = mutableListOf<String>()

    if (normalPressureMmHg != null) {
        val delta = pressureMmHg - normalPressureMmHg
        parts += when {
            abs(delta) < 1 -> "норма"
            else -> "%+.0f к норме".format(delta)
        }
    }

    trend?.let {
        parts += when (it.direction) {
            PressureDirection.RISING -> "растёт на %.0f за %d ч".format(
                abs(it.deltaMmHg),
                TREND_WINDOW_HOURS
            )
            PressureDirection.FALLING -> "падает на %.0f за %d ч".format(
                abs(it.deltaMmHg),
                TREND_WINDOW_HOURS
            )
            PressureDirection.STEADY -> "ровное"
        }
    }

    return parts.joinToString(" · ").ifEmpty { "—" }
}

@Composable
private fun WeatherFact(
    title: String,
    value: String,
    detail: String,
    windDirection: Double? = null
) {
    Column(
        modifier = Modifier.width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            windDirection?.let { degrees ->
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(windArrowRotation(degrees))
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/** Лента часов: значок, вероятность осадков, температура и ветер. */
@Composable
private fun HourlyStrip(window: HourWindow<WeatherEntity>) {
    val listState = rememberLazyListState()

    // Лента открывается на «сейчас»: прошлое рядом, но смотрят вперёд.
    LaunchedEffect(window.nowIndex) {
        if (window.nowIndex > 0) listState.scrollToItem(window.nowIndex)
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(window.items) { index, hour ->
            val past = window.isPast(index)
            val now = index == window.nowIndex

            Column(
                modifier = Modifier
                    .width(64.dp)
                    .background(
                        color = if (now) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .alpha(if (past) PAST_ALPHA else 1f)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (now) "сейчас" else hour.hourLabel(),
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = skyOf(hour.weatherCode).icon(),
                    contentDescription = weatherCodeText(hour.weatherCode),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(22.dp)
                )
                // Ноль процентов рисовать незачем: капля важна там, где есть риск.
                Text(
                    text = hour.precipitationChance.roundToInt()
                        .takeIf { it > 0 }
                        ?.let { "$it%" }
                        ?: " ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "%+d°".format(hour.temperature.roundToInt()),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .rotate(windArrowRotation(hour.windDirection))
                    )
                    Text(
                        text = "%.0f".format(hour.windSpeed.kmhToMs()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Неделя одной картиной: шапка дней и общий график дневных и ночных
 * температур. Всё, что не влияет на решение ехать или нет, убрано.
 */
@Composable
private fun WeekForecast(days: List<DailyForecast>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "Прогноз на ${days.size} дней",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { day ->
                    DayColumnHeader(day, modifier = Modifier.weight(1f))
                }
            }

            DayNightChart(
                dayTemperatures = days.map { it.dayTemperature },
                nightTemperatures = days.map { it.nightTemperature }
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { day ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(12.dp)
                                    .rotate(windArrowRotation(day.windDirection))
                            )
                            Text(
                                text = "%.0f".format(day.windSpeedKmh.kmhToMs()),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            text = "${day.pressureMmHg.roundToInt()} мм",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Стрелка — откуда дует, м/с. Ниже — среднее давление дня.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun DayColumnHeader(day: DailyForecast, modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val isWeekend = day.date.dayOfWeek.value >= 6

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.date.dayOfWeek
                .getDisplayName(java.time.format.TextStyle.SHORT, Locale.forLanguageTag("ru"))
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = if (isWeekend) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = if (day.date == today) "сегодня" else day.date.format(DAY_FORMATTER),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            imageVector = day.sky.icon(),
            contentDescription = weatherCodeText(day.weatherCode),
            modifier = Modifier
                .padding(top = 4.dp)
                .size(22.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (day.precipitationChance > 0) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${day.precipitationChance}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(text = " ", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Локальный барометр против сетевого прогноза: расхождение показывает,
 * насколько прогноз описывает именно ту точку, где стоит рыболов.
 */
@Composable
private fun LocalBarometerCard(
    localPressure: Float?,
    forecastPressureHpa: Double?,
    normalPressureMmHg: Double?,
    dayAgoPressureHpa: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Барометр устройства",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (localPressure == null) {
                Text(
                    text = "Ожидание показаний датчика…",
                    style = MaterialTheme.typography.bodyMedium
                )
                return@Column
            }

            val localMmHg = localPressure.hPaToMmHg()
            Text(
                text = "${localMmHg.roundToInt()} мм рт. ст.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            forecastPressureHpa?.let { forecast ->
                Text(
                    text = "Прогноз по району: %d мм (%+.0f)".format(
                        forecast.hPaToMmHg().roundToInt(),
                        localMmHg - forecast.hPaToMmHg().toFloat()
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            normalPressureMmHg?.let { normal ->
                Text(
                    text = "Норма района: %d мм (%+.0f)".format(
                        normal.roundToInt(),
                        localMmHg - normal.toFloat()
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Собственные показания суточной давности точнее сетевых: они
            // сняты там, где рыболов стоял.
            dayAgoPressureHpa?.let { dayAgo ->
                Text(
                    text = "Сутки назад по датчику: %d мм (%+.0f)".format(
                        dayAgo.hPaToMmHg().roundToInt(),
                        localMmHg - dayAgo.hPaToMmHg().toFloat()
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Показание барометра примерно сутки назад. Ряд рваный — датчик работает
 * только с открытым приложением, поэтому берётся ближайшее к суткам, но не
 * дальше пары часов от них.
 */
private fun List<PressureLogEntity>.pressureDayAgo(): Double? {
    val target = LocalDateTime.now().minusDays(1)
    return minByOrNull { abs(Duration.between(LocalDateTime.parse(it.time), target).toMinutes()) }
        ?.takeIf { abs(Duration.between(LocalDateTime.parse(it.time), target).toHours()) <= 2 }
        ?.pressure
}

/** «05:42» из полной отметки времени, которую отдаёт Open-Meteo. */
private fun String.timeOnly(): String =
    LocalDateTime.parse(this).format(TIME_FORMATTER)

/** Ближайший к заданному моменту час прогноза; null, если история не дошла. */
private fun List<WeatherEntity>.hourNearest(moment: LocalDateTime): WeatherEntity? =
    minByOrNull { abs(Duration.between(LocalDateTime.parse(it.time), moment).toMinutes()) }
        ?.takeIf { abs(Duration.between(LocalDateTime.parse(it.time), moment).toHours()) <= 1 }

private fun WeatherEntity.hourLabel(): String =
    LocalDateTime.parse(time).format(TIME_FORMATTER)

/**
 * Ближайший к текущему времени час прогноза. Прошедшие часы суток остаются
 * в кэше, и без этого отсчёт начинался бы с полуночи.
 */
private fun List<WeatherEntity>.indexOfCurrentHour(): Int {
    if (isEmpty()) return 0
    val now = LocalDateTime.now()
    val index = indexOfFirst { !LocalDateTime.parse(it.time).isBefore(now) }
    // Все часы в прошлом — значит прогноз устарел; показываем последний.
    return if (index >= 0) (index - 1).coerceAtLeast(0) else lastIndex
}
