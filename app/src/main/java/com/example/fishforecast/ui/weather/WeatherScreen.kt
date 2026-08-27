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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.domain.weather.DailyForecast
import com.example.fishforecast.domain.weather.PressureDirection
import com.example.fishforecast.domain.weather.kmhToMs
import com.example.fishforecast.domain.weather.pressureTrend
import com.example.fishforecast.domain.weather.skyOf
import com.example.fishforecast.domain.weather.toDailyForecast
import com.example.fishforecast.domain.weather.weatherCodeText
import com.example.fishforecast.domain.weather.windArrowRotation
import com.example.fishforecast.domain.weather.windDescription
import com.example.fishforecast.domain.weather.windDirectionLabel
import com.example.fishforecast.ui.common.NoActiveMapMessage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** Сколько часов показывать на графиках ближайшего времени. */
private const val HOURLY_WINDOW = 24

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
    val normalPressure by viewModel.normalPressureMmHg.collectAsState()
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
    val upcoming = remember(forecast, currentIndex) {
        forecast.drop(currentIndex).take(HOURLY_WINDOW)
    }
    val days = remember(forecast) { forecast.toDailyForecast() }

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
                            normalPressureMmHg = normalPressure,
                            trend = pressureTrend(upcoming.take(TREND_WINDOW_HOURS))
                        )
                    }

                    if (upcoming.size >= 2) {
                        item {
                            ChartSection(title = "Ближайшие часы") {
                                HourlyStrip(upcoming)
                            }
                        }
                        item {
                            ChartSection(
                                title = "Температура",
                                subtitle = "На $HOURLY_WINDOW часа вперёд"
                            ) {
                                LineChart(
                                    points = upcoming.map { hour ->
                                        ChartPoint(
                                            label = hour.hourLabel(),
                                            value = hour.temperature
                                        )
                                    },
                                    valueSuffix = "°",
                                    highlightIndex = 0
                                )
                            }
                        }
                        item {
                            ChartSection(
                                title = "Давление, мм рт. ст.",
                                subtitle = normalPressure?.let {
                                    "Пунктиром — норма района: ${it.roundToInt()} мм"
                                } ?: "Норму района можно задать в списке карт — " +
                                    "от неё считается клёв"
                            ) {
                                LineChart(
                                    points = upcoming.map { hour ->
                                        ChartPoint(
                                            label = hour.hourLabel(),
                                            value = hour.pressure.hPaToMmHg()
                                        )
                                    },
                                    valueSuffix = "",
                                    lineColor = MaterialTheme.colorScheme.tertiary,
                                    highlightIndex = 0,
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
                                normalPressureMmHg = normalPressure
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
                        windDescription(weather.windSpeed),
                    windDirection = weather.windDirection
                )
                WeatherFact(
                    title = "Влажность",
                    value = "${weather.humidity.roundToInt()}%",
                    detail = "осадки ${weather.precipitationChance.roundToInt()}%"
                )
            }
        }
    }
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
private fun HourlyStrip(hours: List<WeatherEntity>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(hours) { index, hour ->
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .background(
                        color = if (index == 0) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (index == 0) "сейчас" else hour.hourLabel(),
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
    normalPressureMmHg: Double?
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
        }
    }
}

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
