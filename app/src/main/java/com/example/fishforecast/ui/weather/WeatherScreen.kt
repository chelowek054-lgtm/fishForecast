package com.example.fishforecast.ui.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.WeatherEntity
import com.example.fishforecast.domain.sensor.hPaToMmHg
import com.example.fishforecast.ui.common.NoActiveMapMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onOpenMap: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val forecast by viewModel.forecast.collectAsState()
    val activeMap by viewModel.activeMap.collectAsState()
    val localPressure by viewModel.localPressure.collectAsState()
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value

    LaunchedEffect(Unit) {
        if (forecast.isEmpty()) {
            viewModel.loadWeatherInfo()
        }
    }

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
                    modifier = Modifier.padding(16.dp).align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CurrentWeatherHeader(forecast.firstOrNull())
                    }
                    if (viewModel.hasBarometer) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            LocalBarometerCard(
                                localPressure = localPressure,
                                forecastPressure = forecast.firstOrNull()?.pressure
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Прогноз на ближайшее время",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(forecast) { weather ->
                        WeatherForecastItem(weather)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherHeader(weather: WeatherEntity?) {
    weather?.let {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Текущая погода",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${it.temperature.toInt()}°C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Давление: ${it.pressure.toInt()} гПа",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Ветер: ${it.windSpeed} км/ч",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Локальный барометр против сетевого прогноза: расхождение показывает,
 * насколько прогноз описывает именно ту точку, где стоит рыболов.
 */
@Composable
fun LocalBarometerCard(
    localPressure: Float?,
    forecastPressure: Double?
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
            } else {
                Text(
                    text = "${localPressure.toInt()} гПа " +
                        "(${localPressure.hPaToMmHg().toInt()} мм рт. ст.)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (forecastPressure != null) {
                    val delta = localPressure - forecastPressure.toFloat()
                    Text(
                        text = "Отклонение от прогноза: %+.1f гПа".format(delta),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherForecastItem(weather: WeatherEntity) {
    val time = LocalDateTime.parse(weather.time)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = time.format(formatter),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${weather.temperature.toInt()}°C",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${weather.pressure.toInt()} гПа",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}