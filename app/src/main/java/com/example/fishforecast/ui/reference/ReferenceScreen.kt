package com.example.fishforecast.ui.reference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Справочник — база знаний приложения.
 *
 * Раньше эта вкладка была списком рыб. Но расчёт клёва опирается не только
 * на виды: тип водоёма меняет физику воды, структуры делают место местом, а
 * наблюдения с берега поправляют прогноз фактом. Всё это — знание, и жить
 * оно должно рядом, в одном справочнике, откуда расчёт и берёт свои
 * коэффициенты.
 *
 * Разделы стоят по важности: сначала виды, ради которых едут, потом водоём,
 * потом места на нём, потом то, что видно с берега прямо сейчас.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(
    onAddFish: () -> Unit,
    onEditFish: (Int) -> Unit,
    viewModel: ReferenceViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val knowledge by viewModel.knowledgeCatalog.collectAsState()
    val catalogUrl by viewModel.catalogUrl.collectAsState()
    val catalogVersion by viewModel.catalogVersion.collectAsState()
    val knowledgeUrl by viewModel.knowledgeUrl.collectAsState()
    val knowledgeVersion by viewModel.knowledgeVersion.collectAsState()
    val busy = viewModel.busy.value

    val snackbarHostState = remember { SnackbarHostState() }
    var section by remember { mutableStateOf(ReferenceSection.FISH) }
    var showSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Справочник")
                        Text(
                            text = section.subtitle(cards.size, knowledge.version.takeIf { it > 0 }),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSourceDialog = true }) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Источники знаний")
                    }
                }
            )
        },
        floatingActionButton = {
            // Добавлять руками имеет смысл только виды: словари правятся
            // документом, а не по одной строке.
            if (section == ReferenceSection.FISH) {
                FloatingActionButton(onClick = onAddFish) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить вид")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(selectedTabIndex = section.ordinal, edgePadding = 8.dp) {
                ReferenceSection.entries.forEach { item ->
                    Tab(
                        selected = section == item,
                        onClick = { section = item },
                        text = { Text(item.title) }
                    )
                }
            }

            when (section) {
                ReferenceSection.FISH -> FishSection(
                    cards = cards,
                    error = viewModel.error.value,
                    busy = busy,
                    onEditFish = onEditFish,
                    onDeleteFish = viewModel::deleteFish
                )

                ReferenceSection.WATERBODIES -> WaterBodiesSection(knowledge)
                ReferenceSection.STRUCTURES -> StructuresSection(knowledge)
                ReferenceSection.OBSERVATIONS -> ObservationsSection(knowledge)
            }
        }
    }

    if (showSourceDialog) {
        SourcesDialog(
            fishUrl = catalogUrl.orEmpty(),
            fishVersion = catalogVersion,
            knowledgeUrl = knowledgeUrl.orEmpty(),
            knowledgeVersion = knowledgeVersion,
            onDismiss = { showSourceDialog = false },
            onSave = { fish, knowledgeSource ->
                showSourceDialog = false
                viewModel.setCatalogUrl(fish)
                viewModel.setKnowledgeUrl(knowledgeSource)
                viewModel.refreshCatalog()
                viewModel.refreshKnowledge()
            },
            onRestore = {
                showSourceDialog = false
                viewModel.restoreBuiltInCatalog()
                viewModel.restoreBuiltInKnowledge()
            }
        )
    }
}

/** Разделы справочника по важности: вид, водоём, место, факт с берега. */
enum class ReferenceSection(val title: String) {
    FISH("Виды"),
    WATERBODIES("Водоёмы"),
    STRUCTURES("Структуры"),
    OBSERVATIONS("Наблюдения");

    fun subtitle(fishCount: Int, knowledgeVersion: Int?): String = when (this) {
        FISH -> "Кто сегодня берёт · видов: $fishCount"
        WATERBODIES -> "Течение и размер меняют воду и кислород"
        STRUCTURES -> "Что делает место местом"
        OBSERVATIONS -> "Что видно с берега прямо сейчас"
    } + (knowledgeVersion?.let { " · словари v$it" } ?: "")
}

@Composable
private fun SourcesDialog(
    fishUrl: String,
    fishVersion: Int,
    knowledgeUrl: String,
    knowledgeVersion: Int,
    onDismiss: () -> Unit,
    onSave: (fishUrl: String?, knowledgeUrl: String?) -> Unit,
    onRestore: () -> Unit
) {
    var fish by remember { mutableStateOf(fishUrl) }
    var knowledge by remember { mutableStateOf(knowledgeUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Источники знаний") },
        text = {
            Column {
                Text(
                    text = "Знания о рыбе и о водоёмах пополняются быстрее, чем выходят " +
                        "обновления приложения. Укажите адреса — и справочник будет " +
                        "приходить оттуда.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer8()
                OutlinedTextField(
                    value = fish,
                    onValueChange = { fish = it },
                    label = { Text("Справочник видов") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = if (fishVersion > 0) "Скачанная версия: $fishVersion" else
                        "Пока используется встроенный",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer8()
                OutlinedTextField(
                    value = knowledge,
                    onValueChange = { knowledge = it },
                    label = { Text("Словари: водоёмы, структуры, наблюдения") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = if (knowledgeVersion > 0) "Скачанная версия: $knowledgeVersion" else
                        "Пока используются встроенные",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer8()
                Text(
                    text = "Своё описание вида и виды, заведённые вручную, обновление " +
                        "не трогает.",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onRestore) { Text("Вернуть встроенные") }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(fish.takeIf { it.isNotBlank() }, knowledge.takeIf { it.isNotBlank() })
                }
            ) {
                Text("Сохранить и обновить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
private fun Spacer8() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
}
