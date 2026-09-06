package com.example.fishforecast.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Шапка экрана: название раздела, а под ним — выбранный район.
 *
 * Район переключается отсюда, из любого раздела. Раньше за этим надо было
 * уходить в «Сохранённые карты», хотя вопрос «а что на другом пруду»
 * возникает ровно там, где смотришь погоду или клёв, — и возвращаться к
 * нему приходилось по памяти.
 *
 * Погоду переключение не запрашивает заново: она уже лежит в базе для каждой
 * карты, и подставляется сразу. Сеть нужна только чтобы её обновить, а на
 * воде её нет.
 */
@Composable
fun ActiveMapTitle(
    section: String,
    modifier: Modifier = Modifier,
    /** Открыть список карт; null — экран не умеет туда переходить. */
    onOpenLibrary: (() -> Unit)? = null,
    viewModel: ActiveMapViewModel = hiltViewModel()
) {
    val maps by viewModel.savedMaps.collectAsStateWithLifecycle()
    val active by viewModel.activeMap.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = section)

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                // Кликабельна всегда, даже когда карта одна. Скрытая стрелка
                // выглядела так, будто переключателя нет вовсе, — а с одним
                // районом он нужнее всего: из меню заводят второй.
                modifier = Modifier.clickable { expanded = true }
            ) {
                Text(
                    text = active?.name ?: "Район не выбран",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Сменить район",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                maps.forEach { map ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(map.name)
                                // Своя норма давления — признак того, что по
                                // району уже накоплены наблюдения: с ним
                                // расчёт точнее, и это стоит видеть до выбора.
                                map.baselinePressureMmHg?.let { normal ->
                                    Text(
                                        text = "норма %.0f мм".format(normal),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            viewModel.select(map.id)
                        },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        trailingIcon = {
                            if (map.id == active?.id) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Выбран",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }

                // Выход к списку карт: оттуда район заводят, правят и удаляют.
                // Без него меню с одной картой было бы тупиком.
                onOpenLibrary?.let { open ->
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (maps.isEmpty()) "Добавить район" else "Все карты",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            expanded = false
                            open()
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
