package com.example.fishforecast.ui.maps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.ui.map.shareMapsDatabase
import com.example.fishforecast.ui.map.shareSpotsAsGpx
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMapsScreen(
    onOpenMap: () -> Unit = {},
    viewModel: SavedMapsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val maps by viewModel.maps.collectAsState()
    val activeMap by viewModel.activeMap.collectAsState()
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
                is SavedMapsMessage.MapsReady -> shareMapsDatabase(context, event.file)
                is SavedMapsMessage.Info -> snackbarHostState.showSnackbar(event.text)
                is SavedMapsMessage.Error -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Сохранённые карты") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (busy) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            if (maps.isEmpty()) {
                item {
                    EmptyMapsCard(
                        busy = busy,
                        onOpenMap = onOpenMap,
                        onImport = { pickMaps.launch(arrayOf("*/*")) }
                    )
                }
            }

            items(maps) { map ->
                SavedMapCard(
                    map = map,
                    isActive = map.id == activeMap?.id,
                    onSelect = { viewModel.selectMap(map) },
                    onRename = { viewModel.renameMap(map, it) },
                    onNormalPressureChange = { viewModel.setNormalPressure(map, it) },
                    onDepthsChange = { shallow, deep -> viewModel.setDepths(map, shallow, deep) },
                    onRecalculateNormal = { viewModel.recalculateNormalPressure(map) },
                    onWaterMeasured = { viewModel.measureWater(map, it) },
                    onDelete = { viewModel.deleteMap(map) }
                )
            }

            if (maps.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.exportMaps() }, enabled = !busy) {
                            Text("Поделиться картами")
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
                        text = "Чужую карту нужно один раз открыть при интернете: вместе с " +
                            "областью не передаётся оформление стиля. После этого она " +
                            "рисуется без сети.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SpotsCard(
                    spotCount = spots.size,
                    busy = busy,
                    onExport = {
                        scope.launch { shareSpotsAsGpx(context, viewModel.buildSpotsGpx()) }
                    },
                    onImport = { pickGpx.launch(arrayOf("*/*")) }
                )
            }
        }
    }
}

@Composable
private fun EmptyMapsCard(
    busy: Boolean,
    onOpenMap: () -> Unit,
    onImport: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Пока ни одной карты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Выбранная карта задаёт район: по нему грузится погода и считается " +
                    "клёв. Сохраните область на карте или примите готовую от другого рыболова.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenMap) { Text("Открыть карту") }
                OutlinedButton(onClick = onImport, enabled = !busy) { Text("Загрузить чужую") }
            }
        }
    }
}

@Composable
private fun SpotsCard(
    spotCount: Int,
    busy: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Точки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Всего сохранено: $spotCount",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExport, enabled = !busy && spotCount > 0) {
                    Text("Выгрузить GPX")
                }
                OutlinedButton(onClick = onImport, enabled = !busy) { Text("Загрузить GPX") }
            }
        }
    }
}

@Composable
private fun SavedMapCard(
    map: SavedMapEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onRename: (String) -> Unit,
    onNormalPressureChange: (Double?) -> Unit,
    onDepthsChange: (Double?, Double?) -> Unit,
    onRecalculateNormal: () -> Unit,
    onWaterMeasured: (Double?) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isActive) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = map.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isActive) {
                    AssistChip(onClick = {}, label = { Text("Активная") })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Район примерно ${formatDistance(map.widthKm)} × ${formatDistance(map.heightKm)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${map.sizeBytes.asMegabytes()} МБ · сохранена " +
                    DATE_FORMAT.format(Date(map.createdAt)),
                style = MaterialTheme.typography.bodySmall
            )
            // Норма считается сама, поэтому важно не столько число, сколько
            // откуда оно взялось: своё значение рыболова старше расчёта.
            val normal = map.normalPressureMmHg ?: map.baselinePressureMmHg
            normal?.let { value ->
                Text(
                    text = "Норма давления: ${value.toInt()} мм рт. ст. · " +
                        if (map.normalPressureMmHg != null) {
                            "задана вами"
                        } else {
                            "среднее по наблюдениям"
                        },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (map.shallowDepthM != null || map.deepDepthM != null) {
                Text(
                    text = "Глубины: мель %s, яма %s".format(
                        map.shallowDepthM?.let { "%.1f м".format(it) } ?: "—",
                        map.deepDepthM?.let { "%.1f м".format(it) } ?: "—"
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isActive) {
                    Button(onClick = onSelect) { Text("Сделать активной") }
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Свернуть" else "Изменить")
                }
            }

            if (expanded) {
                MapSettings(
                    map = map,
                    onRename = onRename,
                    onNormalPressureChange = onNormalPressureChange,
                    onDepthsChange = onDepthsChange,
                    onRecalculateNormal = onRecalculateNormal,
                    onWaterMeasured = onWaterMeasured,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun MapSettings(
    map: SavedMapEntity,
    onRename: (String) -> Unit,
    onNormalPressureChange: (Double?) -> Unit,
    onDepthsChange: (Double?, Double?) -> Unit,
    onRecalculateNormal: () -> Unit,
    onWaterMeasured: (Double?) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(map.id) { mutableStateOf(map.name) }
    var pressure by remember(map.id) {
        mutableStateOf(map.normalPressureMmHg?.toInt()?.toString().orEmpty())
    }
    var shallow by remember(map.id) { mutableStateOf(map.shallowDepthM.asInput()) }
    var deep by remember(map.id) { mutableStateOf(map.deepDepthM.asInput()) }
    var waterTemp by remember(map.id) { mutableStateOf("") }

    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Название") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = pressure,
        onValueChange = { input -> pressure = input.filter { it.isDigit() } },
        label = { Text("Норма давления, мм рт. ст.") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = map.baselinePressureMmHg?.let { baseline ->
            "Можно не заполнять: по наблюдениям за место выходит " +
                "${baseline.toInt()} мм рт. ст. Своё значение старше расчёта."
        } ?: "Можно не заполнять: приложение посчитает норму места само, " +
            "как только появится сеть.",
        style = MaterialTheme.typography.bodySmall
    )
    TextButton(onClick = onRecalculateNormal) {
        Text("Пересчитать по наблюдениям")
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = shallow,
            onValueChange = { shallow = it.filterDepth() },
            label = { Text("Мель, м") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = deep,
            onValueChange = { deep = it.filterDepth() },
            label = { Text("Яма, м") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
    Text(
        text = "Карт глубин для прудов не существует, а без глубины не посчитать " +
            "температуру воды: мель за ночь остывает и насыщается кислородом, яма — нет.",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = waterTemp,
        onValueChange = { waterTemp = it.filterDepth() },
        label = { Text("Замер воды термометром, °C") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = map.waterTempAt?.let { "Последний замер: ${it.replace('T', ' ')}" }
            ?: "Необязательно: расчёт идёт и без замера, но измеренное всегда точнее.",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                onRename(name)
                onNormalPressureChange(pressure.toDoubleOrNull())
                onDepthsChange(shallow.toDepth(), deep.toDepth())
                waterTemp.toDoubleOrNull()?.let(onWaterMeasured)
            }
        ) {
            Text("Сохранить")
        }
        TextButton(onClick = onDelete) {
            Text("Удалить", color = MaterialTheme.colorScheme.error)
        }
    }
}

/** В поле глубины пускаем только число с одной запятой. */
private fun String.filterDepth(): String =
    replace(',', '.').filter { it.isDigit() || it == '.' }.let { input ->
        val dot = input.indexOf('.')
        if (dot < 0) input else input.substring(0, dot + 1) + input.substring(dot + 1).replace(".", "")
    }

private fun String.toDepth(): Double? = toDoubleOrNull()?.takeIf { it > 0 }

private fun Double?.asInput(): String = this?.let { "%.1f".format(it) }.orEmpty()

private fun Long.asMegabytes(): String = "%.1f".format(this / 1024.0 / 1024.0)

private val DATE_FORMAT = SimpleDateFormat("d MMMM", Locale.forLanguageTag("ru"))
