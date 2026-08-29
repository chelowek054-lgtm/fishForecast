package com.example.fishforecast.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSessionEntity
import com.example.fishforecast.domain.bite.WaterLayerChoice
import com.example.fishforecast.domain.knowledge.FishingMethod
import com.example.fishforecast.domain.session.CatchGoal
import com.example.fishforecast.domain.session.DayPart
import com.example.fishforecast.domain.session.FishingStrategy
import com.example.fishforecast.domain.session.StrategyAdvice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Сборы: анкета и план.
 *
 * Вопросов ровно столько, сколько меняет совет. Каждый ответ сразу
 * пересобирает план — рыболов видит, что выбор способа или места решает не
 * меньше, чем погода.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionSetup(
    form: SessionForm,
    fishList: List<FishEntity>,
    methods: List<FishingMethod>,
    strategy: FishingStrategy?,
    busy: Boolean,
    onChange: ((SessionForm) -> SessionForm) -> Unit,
    onStart: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Собраться на рыбалку",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Несколько вопросов — и приложение соберёт план под эту воду.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "За кем едем", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fishList.forEach { fish ->
                    FilterChip(
                        selected = form.fish?.id == fish.id,
                        onClick = { onChange { it.copy(fish = fish, methodId = null) } },
                        label = { Text(fish.name) }
                    )
                }
            }

            if (methods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Чем ловим", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = form.methodId == method.id,
                            onClick = { onChange { it.copy(methodId = method.id) } },
                            label = { Text(method.name) }
                        )
                    }
                }
                methods.firstOrNull { it.id == form.methodId }?.let { method ->
                    Text(text = method.notes, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (form.fish?.let { it.guild != "predator" } == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "За кем едем", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatchGoal.entries.forEach { goal ->
                        FilterChip(
                            selected = form.goal == goal,
                            onClick = { onChange { it.copy(goal = goal) } },
                            label = { Text(goal.title) }
                        )
                    }
                }
                Text(
                    text = "Стая молодняка и одиночная крупная рыба зовутся разным столом: " +
                        "от ответа зависит и схема закорма, и размер насадки.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.hasGroundbait,
                        onClick = { onChange { it.copy(hasGroundbait = true) } },
                        label = { Text("Прикормка с собой") }
                    )
                    FilterChip(
                        selected = !form.hasGroundbait,
                        onClick = { onChange { it.copy(hasGroundbait = false) } },
                        label = { Text("Без прикормки") }
                    )
                }
            }

            strategy?.let {
                Spacer(modifier = Modifier.height(16.dp))
                StrategyCard(it)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStart,
                enabled = form.fish != null && !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (form.fish == null) "Выберите рыбу" else "Начать рыбалку")
            }
        }
    }
}

/** План: что делать и почему. Совет без причины проверить нельзя. */
@Composable
fun StrategyCard(strategy: FishingStrategy) {
    Column {
        Text(
            text = "План на ${strategy.fish.name}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOfNotNull(
            strategy.place,
            strategy.horizon,
            strategy.bait,
            strategy.backupBait,
            strategy.groundbait,
            strategy.baiting,
            strategy.selection,
            strategy.rig,
            strategy.window
        ).forEach { advice -> AdviceRow(advice) }

        if (strategy.day.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Как пройдут сутки", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "Рыба не стоит на месте: с рассветом выходит кормиться, в полдень " +
                    "уходит пережидать. Вот её ход по часам.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            strategy.day.forEach { part -> DayPartRow(part) }
        }

        if (strategy.lookFor.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Искать на месте", style = MaterialTheme.typography.labelLarge)
            FlowRowChips(strategy.lookFor.map { it.name })

            // Место требует своего от снасти: в коряжнике решает фрикцион, в
            // иле — длина поводка. Об этом узнают на берегу, когда уже поздно.
            strategy.lookFor.filter { it.gearNote.isNotBlank() }.forEach { structure ->
                Text(
                    text = "${structure.name}: ${structure.gearNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        strategy.warnings.forEach { warning ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠ $warning",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Отрезок суток: когда, куда и на какой глубине. */
@Composable
private fun DayPartRow(part: DayPart) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (part.fromTime == part.toTime) part.fromTime else
                "${part.fromTime}–${part.toTime}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(96.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = listOfNotNull(
                    if (part.layer == WaterLayerChoice.SHALLOW) "у берега" else "на глубине",
                    part.horizon.lowercase(),
                    part.waterC?.let { "%.0f°".format(it) }
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = part.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${part.score}",
            style = MaterialTheme.typography.titleSmall,
            color = if (part.score >= 70) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun AdviceRow(advice: StrategyAdvice) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "${advice.title}: ${advice.value}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = advice.reason,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(labels: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEach { label ->
            AssistChip(onClick = {}, label = { Text(label) })
        }
    }
}

/**
 * Рыбалка идёт: план под рукой и кнопка завершения.
 *
 * План показывается тот, что был записан на старте, а не пересчитанный:
 * сверять итог надо с тем, по чему рыболов действовал.
 */
@Composable
fun ActiveSession(
    session: FishingSessionEntity,
    fishName: String?,
    onFinish: (Int?, String, Int?) -> Unit,
    onCancel: () -> Unit
) {
    var showFinish by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рыбалка идёт" + (fishName?.let { ": $it" } ?: ""),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Начали в ${TIME_FORMAT.format(Date(session.startedAt))}",
                style = MaterialTheme.typography.bodySmall
            )

            session.biteScore?.let { score ->
                Text(
                    text = "Клёв на старте: $score" +
                        (session.waterTempC?.let { ", вода %.0f°".format(it) } ?: "") +
                        (session.oxygenMgL?.let { ", кислород %.1f мг/л".format(it) } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (session.plan.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "План", style = MaterialTheme.typography.labelLarge)
                Text(text = session.plan, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showFinish = true }) { Text("Завершить") }
                TextButton(onClick = onCancel) { Text("Отменить выезд") }
            }
        }
    }

    if (showFinish) {
        FinishDialog(
            onDismiss = { showFinish = false },
            onConfirm = { count, note, rating ->
                showFinish = false
                onFinish(count, note, rating)
            }
        )
    }
}

@Composable
private fun FinishDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int?, String, Int?) -> Unit
) {
    var count by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Чем закончилось") },
        text = {
            Column {
                Text(
                    text = "Пустой выезд — такие же данные, как удачный: по ним и видно, " +
                        "где модель ошиблась.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { input -> count = input.filter { it.isDigit() } },
                    label = { Text("Поймано, штук") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Как прошло") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Оценка выезда", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { value ->
                        FilterChip(
                            selected = rating == value,
                            onClick = { rating = value },
                            label = { Text("$value") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(count.toIntOrNull(), note, rating) }) {
                Text("Записать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru"))
