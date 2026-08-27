package com.example.fishforecast.ui.map

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.fishforecast.data.local.entities.FishEntity
import com.example.fishforecast.data.local.entities.FishingSpotEntity
import com.example.fishforecast.data.local.entities.SpotPlacement
import com.example.fishforecast.data.local.entities.SectorEntity
import com.example.fishforecast.data.local.entities.ZoneEntity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.fishforecast.data.local.entities.ZoneKind
import com.example.fishforecast.domain.knowledge.StructureType
import com.example.fishforecast.domain.share.GeoPoint
import com.example.fishforecast.domain.share.decodeOutline
import com.example.fishforecast.data.local.entities.SavedMapEntity
import com.example.fishforecast.data.repository.RegionDownloadState
import com.example.fishforecast.domain.share.GpxWriter
import com.example.fishforecast.ui.maps.formatDistance
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Circle
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.plugins.annotation.FillManager
import org.maplibre.android.plugins.annotation.FillOptions
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.OnCircleClickListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onOpenLibrary: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val userLocation by viewModel.userLocation
    val focusRequests by viewModel.focusRequests
    val locationError by viewModel.locationError
    val activeMap by viewModel.activeMap.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    val outline by viewModel.outline
    val structureTypes by viewModel.structureTypes.collectAsState()
    val drawing by viewModel.drawing
    var showZoneDialog by remember { mutableStateOf(false) }
    val baseLayer by viewModel.baseLayer.collectAsState()
    val downloadState by viewModel.downloadState

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapStyle by remember { mutableStateOf<Style?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newSpotPoint by remember { mutableStateOf<LatLng?>(null) }
    var selectedSpot by remember { mutableStateOf<FishingSpotEntity?>(null) }
    val fishList by viewModel.fishList.collectAsState()
    val spots by viewModel.spots.collectAsState()

    val context = LocalContext.current
    var showShareMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта") },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.selectBaseLayer(
                                if (baseLayer == BaseLayer.SCHEME) {
                                    BaseLayer.SATELLITE
                                } else {
                                    BaseLayer.SCHEME
                                }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Слой: ${baseLayer.title}")
                    }
                    IconButton(onClick = onOpenLibrary) {
                        Icon(Icons.Default.Folder, contentDescription = "Хранилище файлов")
                    }
                    IconButton(onClick = { showShareMenu = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Район целиком: пакет") },
                            enabled = activeMap != null,
                            onClick = {
                                showShareMenu = false
                                viewModel.shareActiveRegion()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Точки файлом GPX") },
                            enabled = spots.isNotEmpty(),
                            onClick = {
                                showShareMenu = false
                                val fishNames = fishList.associate { it.id to it.name }
                                shareSpotsAsGpx(context, GpxWriter.write(spots, fishNames))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Снимок карты") },
                            onClick = {
                                showShareMenu = false
                                map?.snapshot { bitmap -> shareMapSnapshot(context, bitmap) }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (activeMap != null) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (drawing) viewModel.cancelDrawing() else viewModel.startDrawing()
                        }
                    ) {
                        Icon(
                            imageVector = if (drawing) Icons.Default.Close else Icons.Default.Draw,
                            contentDescription = if (drawing) "Отменить обводку" else "Обвести зону"
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                SmallFloatingActionButton(onClick = { viewModel.locateUser() }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Моё местоположение")
                }
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = { showSaveDialog = true }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Сохранить область")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Пакет собирается в фоне, поэтому отправка запускается отдельно
            // от нажатия: иначе системный выбор открылся бы с пустым файлом.
            val packToShare by viewModel.packToShare
            LaunchedEffect(packToShare) {
                packToShare?.let { file ->
                    shareRegionPack(context, file)
                    viewModel.packShared()
                }
            }

            val mapView = rememberMapViewWithLifecycle()

            AndroidView(
                factory = {
                    mapView.getMapAsync { readyMap ->
                        readyMap.addOnMapLongClickListener { point ->
                            newSpotPoint = point
                            true
                        }
                        // Обводка ставит вершины обычным нажатием: длинное
                        // уже занято точкой ловли, и держать палец на каждой
                        // вершине контура было бы мучением.
                        readyMap.addOnMapClickListener { point ->
                            viewModel.addOutlinePoint(point.latitude, point.longitude)
                        }
                        map = readyMap
                    }
                    mapView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Стиль переустанавливается при смене слоя. Аннотации живут
            // внутри стиля, поэтому сначала снимается слой точек и только
            // потом меняется стиль: иначе менеджер аннотаций переживает свой
            // Style и обращается к освобождённой памяти в рендер-треде.
            LaunchedEffect(map, baseLayer) {
                val readyMap = map ?: return@LaunchedEffect

                mapStyle = null
                // Ждём кадр: за него композиция успевает убрать слой точек и
                // вызвать его onDispose.
                withFrameNanos { }

                val builder = when (baseLayer) {
                    BaseLayer.SCHEME -> Style.Builder().fromUri(MapConfig.STYLE_URL)
                    BaseLayer.SATELLITE -> Style.Builder().fromJson(MapConfig.satelliteStyleJson)
                }
                readyMap.setStyle(builder) { style -> mapStyle = style }
            }

            // Камера едет на каждый запрос, а не на каждую новую координату:
            // повторное нажатие кнопки со стоянки должно возвращать карту к
            // рыболову, даже если он не сдвинулся с места.
            LaunchedEffect(map, focusRequests) {
                if (focusRequests == 0) return@LaunchedEffect
                val readyMap = map ?: return@LaunchedEffect
                val (lat, lon) = userLocation ?: return@LaunchedEffect
                readyMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), MapConfig.DEFAULT_ZOOM)
                )
            }

            UserLocationLayer(
                mapView = mapView,
                map = map,
                style = mapStyle,
                location = userLocation
            )

            ZonesLayer(
                mapView = mapView,
                map = map,
                style = mapStyle,
                zones = zones,
                sectors = sectors,
                outline = outline
            )

            SpotsLayer(
                mapView = mapView,
                map = map,
                style = mapStyle,
                spots = spots,
                onSpotClick = { selectedSpot = it }
            )

            locationError?.let { message ->
                LocationErrorBanner(
                    message = message,
                    onDismiss = { viewModel.dismissLocationError() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            if (drawing) {
                DrawingPanel(
                    pointCount = outline.size,
                    onUndo = { viewModel.undoOutlinePoint() },
                    onFinish = { showZoneDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            DownloadStatusBanner(
                state = downloadState,
                onDismiss = { viewModel.dismissDownloadState() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            ActiveMapBanner(
                map = activeMap,
                baseLayer = baseLayer,
                onOpenList = onOpenLibrary,
                onShowOnMap = {
                    activeMap?.let { current ->
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(current.toBounds(), 32)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    // Справа место под плавающие кнопки, иначе плашка уезжает под них.
                    .padding(start = 16.dp, end = 88.dp, bottom = 16.dp)
            )
        }
    }

    if (showSaveDialog) {
        val readyMap = map
        val visibleBounds = readyMap?.projection?.visibleRegion?.latLngBounds
        val zoomRange = readyMap?.let { MapConfig.offlineZoomRange(it.cameraPosition.zoom) }

        SaveRegionDialog(
            // Размер и объём показываются заранее: на сильном приближении в
            // рамку попадает пара сотен метров, а на мелком — область, которая
            // не влезет в лимит MapLibre. Без подсказки и то и другое
            // выясняется только после запуска загрузки.
            areaDescription = visibleBounds?.let { bounds ->
                val width = (bounds.longitudeEast - bounds.longitudeWest) *
                    KM_PER_DEGREE * kotlin.math.cos(Math.toRadians(bounds.latitudeNorth))
                val height = (bounds.latitudeNorth - bounds.latitudeSouth) * KM_PER_DEGREE
                "${formatDistance(width)} × ${formatDistance(height)}"
            },
            tileCount = if (visibleBounds != null && zoomRange != null) {
                estimateTileCount(
                    north = visibleBounds.latitudeNorth,
                    south = visibleBounds.latitudeSouth,
                    east = visibleBounds.longitudeEast,
                    west = visibleBounds.longitudeWest,
                    minZoom = zoomRange.start.toInt(),
                    maxZoom = zoomRange.endInclusive.toInt()
                )
            } else {
                0
            },
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                showSaveDialog = false
                if (readyMap != null) {
                    viewModel.saveVisibleRegion(
                        name = name,
                        bounds = readyMap.projection.visibleRegion.latLngBounds,
                        currentZoom = readyMap.cameraPosition.zoom
                    )
                }
            }
        )
    }

    if (showZoneDialog) {
        SaveZoneDialog(
            pointCount = outline.size,
            parentZone = viewModel.enclosingZone(),
            structureTypes = structureTypes,
            onDismiss = { showZoneDialog = false },
            onConfirm = { name, structureId, asSector ->
                showZoneDialog = false
                viewModel.saveOutline(name, structureId, asSector)
            }
        )
    }

    newSpotPoint?.let { point ->
        AddSpotDialog(
            point = point,
            fishList = fishList,
            onDismiss = { newSpotPoint = null },
            onConfirm = { name, fishId, note, placement ->
                viewModel.addSpot(
                    name = name,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    fishId = fishId,
                    note = note,
                    placement = placement
                )
                newSpotPoint = null
            }
        )
    }

    selectedSpot?.let { spot ->
        val fishName = fishList.firstOrNull { it.id == spot.fishId }?.name
        SpotDetailsDialog(
            spot = spot,
            fishName = fishName,
            onDismiss = { selectedSpot = null },
            onShare = { shareSpotLocation(context, spot, fishName) },
            onDelete = {
                viewModel.deleteSpot(spot)
                selectedSpot = null
            }
        )
    }
}

@Composable
private fun AddSpotDialog(
    point: LatLng,
    fishList: List<FishEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, fishId: Int?, note: String, placement: SpotPlacement) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var fishId by remember { mutableStateOf<Int?>(null) }
    var placement by remember { mutableStateOf(SpotPlacement.WATER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая точка") },
        text = {
            Column {
                Text(
                    text = "%.5f, %.5f".format(point.latitude, point.longitude),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") }
                )
                // Точка на берегу и точка в воде — разные вещи: первая
                // говорит, где встать, вторая — куда забрасывать.
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpotPlacement.entries.forEach { option ->
                        FilterChip(
                            selected = placement == option,
                            onClick = { placement = option },
                            label = { Text(if (option == SpotPlacement.SHORE) "Берег" else "Вода") }
                        )
                    }
                }

                if (fishList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Кто здесь берёт:", style = MaterialTheme.typography.labelMedium)
                    LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                        items(fishList) { fish ->
                            TextButton(onClick = { fishId = if (fishId == fish.id) null else fish.id }) {
                                Text(
                                    text = if (fishId == fish.id) "✓ ${fish.name}" else fish.name,
                                    fontWeight = if (fishId == fish.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.ifBlank { "Точка" }, fishId, note, placement)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun SpotDetailsDialog(
    spot: FishingSpotEntity,
    fishName: String?,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(spot.name) },
        text = {
            Column {
                Text(
                    text = "%.5f, %.5f".format(spot.latitude, spot.longitude),
                    style = MaterialTheme.typography.bodySmall
                )
                if (fishName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Здесь берёт: $fishName")
                }
                if (spot.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(spot.note)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onShare) { Text("Поделиться") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("Закрыть") }
            }
        }
    )
}

/**
 * Зоны и секторы поверх карты.
 *
 * Заливка полупрозрачная: под ней должно быть видно берег и рельеф, ради
 * которых контур и рисовался. Секторы обводятся линией — их читают как
 * разметку внутри зоны, а не как отдельные пятна.
 */
@Composable
private fun ZonesLayer(
    mapView: MapView,
    map: MapLibreMap?,
    style: Style?,
    zones: List<ZoneEntity>,
    sectors: List<SectorEntity>,
    outline: List<GeoPoint>
) {
    val readyMap = map ?: return
    val readyStyle = style ?: return

    val fillManager = remember(readyMap, readyStyle) {
        FillManager(mapView, readyMap, readyStyle)
    }
    val lineManager = remember(readyMap, readyStyle) {
        LineManager(mapView, readyMap, readyStyle)
    }

    // Аннотации живут в нативном слое и переживают свой Style, если их не
    // снять руками, — тогда рендер-тред обращается к освобождённой памяти.
    DisposableEffect(fillManager, lineManager) {
        onDispose {
            fillManager.onDestroy()
            lineManager.onDestroy()
        }
    }

    DisposableEffect(fillManager, lineManager, zones, sectors, outline) {
        fillManager.deleteAll()
        lineManager.deleteAll()

        zones.forEach { zone ->
            val points = zone.outline.decodeOutline().map { LatLng(it.latitude, it.longitude) }
            if (points.size < 3) return@forEach
            val water = zone.kind.equals(ZoneKind.WATER.name, ignoreCase = true)
            val color = if (water) "#1E88E5" else "#8D6E63"
            fillManager.create(
                FillOptions()
                    .withLatLngs(listOf(points))
                    .withFillColor(color)
                    .withFillOpacity(0.18f)
            )
            lineManager.create(
                LineOptions()
                    .withLatLngs(points + points.first())
                    .withLineColor(color)
                    .withLineWidth(2f)
            )
        }

        sectors.forEach { sector ->
            val points = sector.outline.decodeOutline().map { LatLng(it.latitude, it.longitude) }
            if (points.size < 3) return@forEach
            lineManager.create(
                LineOptions()
                    .withLatLngs(points + points.first())
                    .withLineColor("#FBC02D")
                    .withLineWidth(1.5f)
            )
        }

        // Контур в работе показывается ломаной: пока он не замкнут, это ещё
        // не зона, и заливать его нечестно.
        if (outline.size >= 2) {
            lineManager.create(
                LineOptions()
                    .withLatLngs(outline.map { LatLng(it.latitude, it.longitude) })
                    .withLineColor("#D32F2F")
                    .withLineWidth(2.5f)
            )
        }

        onDispose { }
    }
}

/** Панель обводки: сколько вершин поставлено и что с ними можно сделать. */
@Composable
private fun DrawingPanel(
    pointCount: Int,
    onUndo: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Нажимайте по карте, обводя границу",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Вершин: $pointCount" + if (pointCount < 3) " — нужно хотя бы три" else "",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onUndo, enabled = pointCount > 0) { Text("Убрать последнюю") }
                TextButton(onClick = onFinish, enabled = pointCount >= 3) { Text("Замкнуть") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SaveZoneDialog(
    pointCount: Int,
    parentZone: ZoneEntity?,
    structureTypes: List<StructureType>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, structureId: String, asSector: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    // Тип зоны — структура из словаря знаний: не просто «вода», а заводь,
    // коряжник или выход ключа. Ради этого зоны и рисуются.
    var structureId by remember { mutableStateOf(ZoneKind.WATER.name.lowercase()) }
    // Контур внутри чужой зоны почти всегда сектор: на водоёмах места
    // нумеруют внутри участков, а не поверх них.
    var asSector by remember { mutableStateOf(parentZone != null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (asSector) "Новый сектор" else "Новая зона") },
        text = {
            Column {
                Text(
                    text = "Обведено вершин: $pointCount",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (asSector) "Номер или имя сектора" else "Название зоны") },
                    singleLine = true
                )

                parentZone?.let { zone ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = asSector,
                            onClick = { asSector = true },
                            label = { Text("Сектор «${zone.name}»") }
                        )
                        FilterChip(
                            selected = !asSector,
                            onClick = { asSector = false },
                            label = { Text("Отдельная зона") }
                        )
                    }
                }

                if (!asSector) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Что это за место", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        structureTypes.forEach { option ->
                            FilterChip(
                                selected = structureId == option.id,
                                onClick = { structureId = option.id },
                                label = { Text(option.name) }
                            )
                        }
                    }
                    Text(
                        text = structureTypes.firstOrNull { it.id == structureId }?.notes.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name.ifBlank { if (asSector) "Сектор" else "Зона" },
                        structureId,
                        asSector
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/**
 * Метка «я здесь»: точка с ореолом поверх карты.
 *
 * Рисуется тем же CircleManager, что и точки ловли, — свой стиль и ассеты
 * ей не нужны, а значит метка одинаково видна на схеме и на спутнике.
 */
@Composable
private fun UserLocationLayer(
    mapView: MapView,
    map: MapLibreMap?,
    style: Style?,
    location: Pair<Double, Double>?
) {
    val readyMap = map ?: return
    val readyStyle = style ?: return

    val circleManager = remember(readyMap, readyStyle) {
        CircleManager(mapView, readyMap, readyStyle)
    }

    // Аннотации живут в нативном слое и переживают свой Style, если их не
    // снять руками, — тогда рендер-тред обращается к освобождённой памяти.
    DisposableEffect(circleManager) {
        onDispose { circleManager.onDestroy() }
    }

    DisposableEffect(circleManager, location) {
        circleManager.deleteAll()
        location?.let { (lat, lon) ->
            val point = LatLng(lat, lon)
            circleManager.create(
                CircleOptions()
                    .withLatLng(point)
                    .withCircleRadius(18f)
                    .withCircleColor("#1E88E5")
                    .withCircleOpacity(0.18f)
            )
            circleManager.create(
                CircleOptions()
                    .withLatLng(point)
                    .withCircleRadius(7f)
                    .withCircleColor("#1E88E5")
                    .withCircleStrokeWidth(2.5f)
                    .withCircleStrokeColor("#FFFFFF")
            )
        }
        onDispose { }
    }
}

/** Молчаливый отказ геолокации выглядит как сломанная кнопка. */
@Composable
private fun LocationErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) { Text("Понятно") }
        }
    }
}

/**
 * Слой секретных точек. CircleManager рисует их без растровых иконок —
 * значит слой не тянет за собой ассеты и одинаково выглядит на любом стиле.
 */
@Composable
private fun SpotsLayer(
    mapView: MapView,
    map: MapLibreMap?,
    style: Style?,
    spots: List<FishingSpotEntity>,
    onSpotClick: (FishingSpotEntity) -> Unit
) {
    val readyMap = map ?: return
    val readyStyle = style ?: return

    val circleManager = remember(readyMap, readyStyle) {
        CircleManager(mapView, readyMap, readyStyle)
    }

    // Аннотации живут в нативном слое, поэтому их надо снимать руками.
    DisposableEffect(circleManager) {
        onDispose { circleManager.onDestroy() }
    }

    DisposableEffect(circleManager, spots) {
        circleManager.deleteAll()
        val circleToSpot = mutableMapOf<Long, FishingSpotEntity>()

        spots.forEach { spot ->
            val circle: Circle = circleManager.create(
                CircleOptions()
                    .withLatLng(LatLng(spot.latitude, spot.longitude))
                    .withCircleRadius(9f)
                    .withCircleColor("#D32F2F")
                    .withCircleStrokeWidth(2f)
                    .withCircleStrokeColor("#FFFFFF")
            )
            circleToSpot[circle.id] = spot
        }

        val listener = object : OnCircleClickListener {
            override fun onAnnotationClick(circle: Circle): Boolean {
                circleToSpot[circle.id]?.let(onSpotClick)
                return true
            }
        }
        circleManager.addClickListener(listener)

        onDispose { circleManager.removeClickListener(listener) }
    }
}

private fun SavedMapEntity.toBounds(): LatLngBounds =
    LatLngBounds.Builder()
        .include(LatLng(north, east))
        .include(LatLng(south, west))
        .build()

@Composable
private fun SaveRegionDialog(
    areaDescription: String?,
    tileCount: Long,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val tooLarge = tileCount > TILE_LIMIT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранить область") },
        text = {
            Column {
                Text("Скачается то, что сейчас видно на экране, вместе с более крупными масштабами.")
                areaDescription?.let { size ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Размер района: $size",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (tooLarge) {
                    Text(
                        text = "Область слишком большая: примерно $tileCount тайлов при " +
                            "пределе $TILE_LIMIT. Приблизьте карту и сохраните участок поменьше.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название района") },
                    singleLine = true
                )
                Text(
                    text = "Норма давления района посчитается сама — по среднему за " +
                        "последние месяцы наблюдений. От неё считается клёв.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !tooLarge,
                onClick = {
                    onConfirm(name.ifBlank { "Мой район" })
                }
            ) {
                Text("Скачать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun DownloadStatusBanner(
    state: RegionDownloadState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == null) return

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (state) {
                is RegionDownloadState.InProgress -> {
                    Text("Скачивание области: ${state.percent}%")
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.percent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is RegionDownloadState.Done -> {
                    Text("Область сохранена (${state.sizeBytes / 1024} КБ)")
                    TextButton(onClick = onDismiss) { Text("Понятно") }
                }

                is RegionDownloadState.Failed -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onDismiss) { Text("Закрыть") }
                }
            }
        }
    }
}

/**
 * Какой район сейчас активен: от него зависят погода и клёв, поэтому это
 * первое, что рыболов должен видеть на карте.
 */
@Composable
private fun ActiveMapBanner(
    map: SavedMapEntity?,
    baseLayer: BaseLayer,
    onOpenList: () -> Unit,
    onShowOnMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (map == null) {
                Text(
                    text = "Район не выбран",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Сохраните область — по ней пойдут погода и расчёт клёва.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = map.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Активный район · ${map.sizeBytes / 1024 / 1024} МБ · ${baseLayer.title}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (baseLayer == BaseLayer.SATELLITE) {
                    // Офлайн сохраняется схема, поэтому про снимки честно
                    // предупреждаем заранее, а не показываем пустой экран.
                    Text(
                        text = "Снимки грузятся из сети",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    TextButton(onClick = onShowOnMap) { Text("Показать") }
                    TextButton(onClick = onOpenList) { Text("Все карты") }
                }
            }
        }
    }
}

/**
 * MapView живёт вне Compose и требует ручной прокачки жизненного цикла,
 * иначе рендер продолжает работать в фоне и течёт память.
 */
@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply { onCreate(Bundle()) }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                // ON_DESTROY намеренно не обрабатывается: уничтожение идёт
                // ниже, в onDispose. Иначе onDestroy вызывается дважды —
                // нативные ресурсы освобождаются повторно, и рендер-тред
                // падает с SIGSEGV.
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    return mapView
}

/** Длина градуса широты в километрах — для оценки размера рамки. */
private const val KM_PER_DEGREE = 111.32
