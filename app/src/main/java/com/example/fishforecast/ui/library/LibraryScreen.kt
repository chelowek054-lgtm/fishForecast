package com.example.fishforecast.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.ui.map.shareMapsDatabase
import com.example.fishforecast.ui.map.shareSpotsAsGpx
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val regions by viewModel.regions.collectAsState()
    val spots by viewModel.spots.collectAsState()
    val busy = viewModel.busy.value
    val snackbarHostState = remember { SnackbarHostState() }

    // Чужие файлы приходят через системный выбор — приложению не нужен
    // доступ ко всему хранилищу.
    val pickMaps = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importMaps) }

    val pickGpx = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importSpots(it) { source ->
                context.contentResolver.openInputStream(source)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryMessage.MapsReady -> shareMapsDatabase(context, event.file)
                is LibraryMessage.Info -> snackbarHostState.showSnackbar(event.text)
                is LibraryMessage.Error -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Хранилище") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (busy) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            item {
                SectionCard(
                    title = "Карты",
                    subtitle = if (regions.isEmpty()) {
                        "Скачанных карт нет. Сохраните область на экране карты или загрузите чужую."
                    } else {
                        "Скачано областей: ${regions.size} · ${regions.sumOf { it.sizeBytes } / 1024} КБ"
                    }
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.exportMaps() },
                            enabled = !busy && regions.isNotEmpty()
                        ) {
                            Text("Выгрузить")
                        }
                        OutlinedButton(
                            onClick = { pickMaps.launch(arrayOf("*/*")) },
                            enabled = !busy
                        ) {
                            Text("Загрузить чужие")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Чужую карту нужно один раз открыть при интернете: " +
                            "вместе с областью не передаётся оформление стиля. " +
                            "После этого она рисуется без сети.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            items(regions) { region ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(region.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Масштабы ${region.minZoom.toInt()}–${region.maxZoom.toInt()} · " +
                                "${region.sizeBytes / 1024} КБ",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard(
                    title = "Точки",
                    subtitle = "Сохранено точек: ${spots.size}"
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    shareSpotsAsGpx(context, viewModel.buildSpotsGpx())
                                }
                            },
                            enabled = !busy && spots.isNotEmpty()
                        ) {
                            Text("Выгрузить GPX")
                        }
                        OutlinedButton(
                            onClick = { pickGpx.launch(arrayOf("*/*")) },
                            enabled = !busy
                        ) {
                            Text("Загрузить GPX")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    actions: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            actions()
        }
    }
}
