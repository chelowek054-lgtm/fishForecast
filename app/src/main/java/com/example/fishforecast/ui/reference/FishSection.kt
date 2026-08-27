package com.example.fishforecast.ui.reference

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.domain.fish.decodeBaits
import com.example.fishforecast.domain.fish.decodeGroundbait
import kotlin.math.roundToInt

/**
 * Раздел «Виды»: справочник рыб, сопоставленный с сегодняшней водой.
 *
 * Сам список ничего не решает — решает порядок: сверху те, кто сейчас
 * кормится. Поэтому раздел получает уже посчитанные карточки, а не сырые
 * записи справочника.
 */
@Composable
fun FishSection(
    cards: List<FishCard>,
    error: String?,
    busy: Boolean,
    onEditFish: (Int) -> Unit,
    onDeleteFish: (FishEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (cards.isEmpty()) {
            Text(
                text = error ?: "Справочник пуст. Добавьте первый вид.",
                color = if (error != null) MaterialTheme.colorScheme.error else Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (busy) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            items(cards, key = { it.fish.id }) { card ->
                FishCardItem(
                    card = card,
                    onEdit = { onEditFish(card.fish.id) },
                    onDelete = { onDeleteFish(card.fish) }
                )
            }
        }
    }
}

/**
 * Карточка вида.
 *
 * Наверху — ответ на главный вопрос: берёт ли эта рыба сейчас. Ниже —
 * почему: где её оптимум относительно нынешней воды, хватает ли ей
 * кислорода, на каком горизонте искать. Стол раскрывается по нажатию:
 * наживки и прикормка нужны уже на воде, а не при выборе, за кем ехать.
 */
@Composable
private fun FishCardItem(
    card: FishCard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val fish = card.fish

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fish.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.verdict(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                card.score?.let { score -> ScoreBadge(score) }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TemperatureBand(
                optMin = fish.optMinTemp.toDouble(),
                optMax = fish.optMaxTemp.toDouble(),
                absMin = fish.absMinTemp.toDouble(),
                absMax = fish.absMaxTemp.toDouble(),
                current = card.waterTemperature
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(horizonText(fish.defaultHorizon)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(card.oxygenText()) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (card.oxygenShort()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (card.coldTable) "Холодный стол" else "Тёплый стол",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Чем кормить и на что ловить"
                )
            }

            AnimatedVisibility(visible = expanded) {
                FishTable(card = card, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

/** Наживки и прикормка под нынешнюю воду. */
@Composable
private fun FishTable(
    card: FishCard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fish = card.fish
    val baits = (if (card.coldTable) fish.baitsCold else fish.baitsWarm).decodeBaits()
    val rule = (if (card.coldTable) fish.groundbaitCold else fish.groundbaitWarm).decodeGroundbait()

    Column(modifier = Modifier.padding(top = 8.dp)) {
        if (fish.description.isNotBlank()) {
            Text(text = fish.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "Вода холоднее ${fish.coldTempThreshold.roundToInt()}° — животные насадки, " +
                "теплее — растительные",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Наживки", style = MaterialTheme.typography.labelLarge)
        if (baits.isEmpty()) {
            Text(
                text = "Не заполнены — обновите справочник или впишите свои",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            baits.forEach { bait ->
                Text(text = "• $bait", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Прикормка", style = MaterialTheme.typography.labelLarge)
        Text(text = rule.summary(), style = MaterialTheme.typography.bodyMedium)
        if (rule.notes.isNotBlank()) {
            Text(
                text = rule.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Давление: ${fish.minPressure.roundToInt()}–${fish.maxPressure.roundToInt()} " +
                "мм рт. ст. · кислород от ${fish.oxygenComfortMgL} мг/л",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Изменить")
            }
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Полоса температур: предел выносливости бледной заливкой, оптимум —
 * насыщенной, метка — нынешняя вода. Одним взглядом видно, попадает ли
 * водоём в комфорт вида.
 */
@Composable
private fun TemperatureBand(
    optMin: Double,
    optMax: Double,
    absMin: Double,
    absMax: Double,
    current: Double?
) {
    val toleranceColor = MaterialTheme.colorScheme.surfaceVariant
    val optimumColor = MaterialTheme.colorScheme.primary
    val markerColor = MaterialTheme.colorScheme.error

    // Шкала общая для всех видов: иначе карточки нельзя сравнить глазами.
    val scaleFrom = SCALE_FROM
    val scaleTo = SCALE_TO

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            fun x(value: Double): Float =
                (((value - scaleFrom) / (scaleTo - scaleFrom)).coerceIn(0.0, 1.0) * size.width)
                    .toFloat()

            drawRoundRect(
                color = toleranceColor,
                topLeft = Offset(x(absMin), size.height * 0.25f),
                size = Size(x(absMax) - x(absMin), size.height * 0.5f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = optimumColor.copy(alpha = 0.55f),
                topLeft = Offset(x(optMin), 0f),
                size = Size(x(optMax) - x(optMin), size.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            current?.let { water ->
                drawCircle(
                    color = markerColor,
                    radius = size.height * 0.32f,
                    center = Offset(x(water), size.height / 2)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${absMin.roundToInt()}°",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "оптимум ${optMin.roundToInt()}–${optMax.roundToInt()}°",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "${absMax.roundToInt()}°",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    val color = when {
        score >= 70 -> MaterialTheme.colorScheme.primary
        score >= 40 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/** Что означает эта рыба для сегодняшней воды — одной строкой. */
private fun FishCard.verdict(): String {
    val water = waterTemperature ?: return "Выберите район — покажу, берёт ли сейчас"
    val fish = fish
    return when {
        water < fish.absMinTemp -> "Вода ${water.roundToInt()}° — холодно до оцепенения"
        water > fish.absMaxTemp -> "Вода ${water.roundToInt()}° — жарко до оцепенения"
        water < fish.optMinTemp -> "Вода ${water.roundToInt()}° — холоднее оптимума"
        water > fish.optMaxTemp -> "Вода ${water.roundToInt()}° — теплее оптимума"
        else -> "Вода ${water.roundToInt()}° — оптимум вида"
    }
}

private fun FishCard.oxygenText(): String {
    val oxygen = oxygenMgL ?: return "кислород ≥ ${fish.oxygenComfortMgL} мг/л"
    return "кислород %.1f из %.1f мг/л".format(oxygen, fish.oxygenComfortMgL)
}

private fun FishCard.oxygenShort(): Boolean =
    (oxygenMgL ?: Double.MAX_VALUE) < fish.oxygenComfortMgL

/** Границы общей шкалы: холоднее и теплее пресная рыба не живёт. */
private const val SCALE_FROM = 0.0
private const val SCALE_TO = 32.0
