package com.example.fishforecast.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fishforecast.domain.knowledge.BaitingPlan
import com.example.fishforecast.domain.knowledge.KnowledgeCatalog
import com.example.fishforecast.domain.knowledge.ObservationType
import com.example.fishforecast.domain.knowledge.StructureType
import com.example.fishforecast.domain.knowledge.WaterBodyType
import kotlin.math.roundToInt

/**
 * Раздел «Водоёмы»: почему течение и размер вообще важны.
 *
 * Цифры показаны намеренно — это те самые коэффициенты, по которым считается
 * вода и кислород. Рыболов должен видеть, из чего складывается ответ, иначе
 * подсказка выглядит гаданием.
 */
@Composable
fun WaterBodiesSection(catalog: KnowledgeCatalog, modifier: Modifier = Modifier) {
    KnowledgeList(
        items = catalog.waterbodies,
        empty = "Словарь водоёмов пуст",
        modifier = modifier
    ) { type ->
        WaterBodyCard(type)
    }
}

@Composable
private fun WaterBodyCard(type: WaterBodyType) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = type.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOf(
                    if (type.flowing) "с течением" else "стоячий",
                    if (type.large) "большой" else "малый"
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (!type.behaviorDefined) {
                Text(
                    text = "Поведение не описано: расчёт этот тип пока не поддерживает.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("инерция ×%.1f".format(type.thermalInertia)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("кислород ${(type.aeration * 100).roundToInt()}%") }
                    )
                }
                if (type.nightOxygenDropMgL > 0) {
                    Text(
                        text = "К рассвету кислорода меньше на %.1f мг/л"
                            .format(type.nightOxygenDropMgL),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (type.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = type.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Раздел «Структуры»: что делает место местом. */
@Composable
fun StructuresSection(catalog: KnowledgeCatalog, modifier: Modifier = Modifier) {
    KnowledgeList(
        items = catalog.structures.sortedByDescending { maxOf(it.predatorBonus, it.peacefulBonus) },
        empty = "Словарь структур пуст",
        modifier = modifier
    ) { structure ->
        StructureCard(structure)
    }
}

@Composable
private fun StructureCard(structure: StructureType) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = structure.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (structure.gives.isNotEmpty()) {
                Text(
                    text = structure.gives.joinToString(" · ") { givesText(it) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("хищнику %+d%%".format((structure.predatorBonus * 100).roundToInt())) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("мирной %+d%%".format((structure.peacefulBonus * 100).roundToInt())) }
                )
            }
            if (structure.oxygenBonusMgL != 0.0) {
                Text(
                    text = "Кислород %+.1f мг/л".format(structure.oxygenBonusMgL),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (structure.waterOffsetC != 0.0) {
                Text(
                    text = "Вода %+.1f °C к общей".format(structure.waterOffsetC),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (structure.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = structure.notes, style = MaterialTheme.typography.bodyMedium)
            }

            if (structure.gearNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Снасть: ${structure.gearNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Раздел «Закорм»: как корм ложится на дно.
 *
 * Состав стола задаёт справочник вида — он зависит от температуры воды.
 * Схема отвечает на другой вопрос: тот же корм ковром и точкой зовёт разную
 * рыбу, и вместе со схемой меняется размер насадки.
 */
@Composable
fun BaitingSection(catalog: KnowledgeCatalog, modifier: Modifier = Modifier) {
    KnowledgeList(
        items = catalog.baitingPlans,
        empty = "Словарь схем закорма пуст",
        modifier = modifier,
        header = {
            Text(
                text = "Мелкая и средняя рыба ходит стаей и кормится наперегонки, крупная " +
                    "держится одиночкой и осматривает точку. Схема выбирается под ту, за " +
                    "которой едут, и под воду — в холодной работает только точка.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) { plan ->
        BaitingPlanCard(plan)
    }
}

@Composable
private fun BaitingPlanCard(plan: BaitingPlan) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(goalText(plan.goal)) })
                plan.water?.let { water ->
                    AssistChip(onClick = {}, label = { Text(waterText(water)) })
                }
                plan.waterSize?.let { size ->
                    AssistChip(onClick = {}, label = { Text(waterSizeText(size)) })
                }
            }
            if (plan.baitSizeMm.isNotBlank()) {
                Text(
                    text = "Насадка: ${plan.baitSizeMm}" +
                        if (plan.hardened) ", сушить до каменной твёрдости" else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (plan.primeDays > 0) {
                Text(
                    text = "Кормить ${plan.primeDays} дней до ловли, не ловя",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (plan.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = plan.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Раздел «Наблюдения»: факт с берега против прогноза. */
@Composable
fun ObservationsSection(catalog: KnowledgeCatalog, modifier: Modifier = Modifier) {
    KnowledgeList(
        items = catalog.observations,
        empty = "Словарь наблюдений пуст",
        modifier = modifier,
        header = {
            Text(
                text = "Это не прогноз, а то, что видно своими глазами. Такие отметки " +
                    "действуют несколько часов и меняют подсказку отдельной строкой, " +
                    "не подмешиваясь в расчёт молча.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) { observation ->
        ObservationCard(observation)
    }
}

@Composable
private fun ObservationCard(observation: ObservationType) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = observation.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("%+d%%".format((observation.effect * 100).roundToInt())) }
                )
                AssistChip(onClick = {}, label = { Text("${observation.hours} ч") })
                AssistChip(onClick = {}, label = { Text(guildText(observation.guild)) })
            }
            if (observation.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = observation.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun <T> KnowledgeList(
    items: List<T>,
    empty: String,
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    item: @Composable (T) -> Unit
) {
    if (items.isEmpty()) {
        Text(text = empty, modifier = modifier.padding(16.dp))
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        header?.let { content -> item { content() } }
        items(items) { entry -> item(entry) }
    }
}

private fun givesText(value: String): String = when (value) {
    "shelter" -> "укрытие"
    "food" -> "корм"
    "shade" -> "тень"
    "oxygen" -> "кислород"
    "depth" -> "глубина"
    "calm" -> "тихая вода"
    "thermal_refuge" -> "убежище от жары"
    "oxygen_deficit" -> "нехватка кислорода"
    else -> value
}

private fun goalText(value: String): String = when (value) {
    "trophy" -> "за трофеем"
    "numbers" -> "за количеством"
    else -> "любая цель"
}

private fun waterText(value: String): String = when (value) {
    "cold" -> "холодная вода"
    "warm" -> "тёплая вода"
    else -> value
}

private fun waterSizeText(value: String): String = when (value) {
    "large" -> "большая вода"
    "small" -> "пруд, малое озеро"
    else -> value
}

private fun guildText(value: String): String = when (value) {
    "predator" -> "хищник"
    "peaceful" -> "мирная"
    else -> "любая рыба"
}
