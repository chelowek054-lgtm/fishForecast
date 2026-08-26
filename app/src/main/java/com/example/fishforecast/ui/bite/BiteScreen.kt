package com.example.fishforecast.ui.bite

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.domain.bite.BiteForecast
import com.example.fishforecast.domain.bite.BiteLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiteScreen(
    viewModel: BiteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Клёв") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.fishList.isEmpty()) {
                Text("Справочник рыб пуст — добавьте рыбу, чтобы считать активность.")
                return@Column
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.fishList.forEach { fish ->
                    FilterChip(
                        selected = fish.id == state.selectedFish?.id,
                        onClick = { viewModel.selectFish(fish) },
                        label = { Text(fish.name) }
                    )
                }
            }

            if (state.spots.isNotEmpty()) {
                Text("Водоём:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.spots.forEach { spot ->
                        FilterChip(
                            selected = spot.id == state.selectedSpot?.id,
                            onClick = {
                                viewModel.selectSpot(spot.takeIf { it.id != state.selectedSpot?.id })
                            },
                            label = { Text(spot.name) }
                        )
                    }
                }
                state.selectedSpot?.normalPressureMmHg?.let { normal ->
                    Text(
                        text = "Норма давления водоёма: ${normal.toInt()} мм рт. ст.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.weatherMissing) {
                Text("Нет прогноза погоды. Откройте вкладку «Погода», чтобы загрузить его.")
                return@Column
            }

            val current = state.forecast.firstOrNull()
            if (current != null) {
                CurrentBiteCard(current, state.selectedFish?.name.orEmpty())
            }

            Text(
                text = "Активность по часам",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            BiteChart(forecast = state.forecast)
        }
    }
}

@Composable
private fun CurrentBiteCard(forecast: BiteForecast, fishName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = forecast.level.container())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = fishName,
                style = MaterialTheme.typography.labelLarge
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${forecast.score}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = forecast.level.title(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            forecast.factors.forEach { factor ->
                Text(
                    text = "${factor.name}: ${factor.comment}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Столбики активности по часам. Рисуются вручную: библиотека графиков ради
 * одной диаграммы утянула бы в офлайн-приложение лишний вес.
 */
@Composable
private fun BiteChart(forecast: List<BiteForecast>) {
    if (forecast.isEmpty()) return

    val visible = forecast.take(HOURS_ON_CHART)
    val barColors = visible.map { it.level.bar() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            visible.forEachIndexed { index, hour ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barHeight = size.height * (hour.score / 100f)
                        drawRect(
                            color = barColors[index],
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barHeight),
                            size = androidx.compose.ui.geometry.Size(size.width, barHeight)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            visible.forEach { hour ->
                Text(
                    text = hour.time.takeLast(5).take(2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}

private fun BiteLevel.title(): String = when (this) {
    BiteLevel.GOOD -> "Хороший клёв"
    BiteLevel.MODERATE -> "Средний клёв"
    BiteLevel.POOR -> "Клёва почти нет"
}

private fun BiteLevel.container(): Color = when (this) {
    BiteLevel.GOOD -> Color(0xFFB8E4C2)
    BiteLevel.MODERATE -> Color(0xFFF5E6B2)
    BiteLevel.POOR -> Color(0xFFF0C9C9)
}

private fun BiteLevel.bar(): Color = when (this) {
    BiteLevel.GOOD -> Color(0xFF2E7D32)
    BiteLevel.MODERATE -> Color(0xFFF9A825)
    BiteLevel.POOR -> Color(0xFFC62828)
}

private const val HOURS_ON_CHART = 12
