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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.fishforecast.data.local.entities.MapRegionEntity
import com.example.fishforecast.data.repository.RegionDownloadState
import com.example.fishforecast.domain.share.GpxWriter
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Circle
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.plugins.annotation.OnCircleClickListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val userLocation by viewModel.userLocation
    val regions by viewModel.regions.collectAsState()
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
                    IconButton(onClick = { showShareMenu = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false }
                    ) {
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
            val mapView = rememberMapViewWithLifecycle()

            AndroidView(
                factory = {
                    mapView.getMapAsync { readyMap ->
                        readyMap.setStyle(MapConfig.STYLE_URL) { style ->
                            mapStyle = style
                        }
                        readyMap.addOnMapLongClickListener { point ->
                            newSpotPoint = point
                            true
                        }
                        map = readyMap
                    }
                    mapView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Камера ждёт и готовую карту, и координаты: что придёт последним,
            // то и запускает центрирование.
            LaunchedEffect(map, userLocation) {
                val readyMap = map ?: return@LaunchedEffect
                val (lat, lon) = userLocation ?: return@LaunchedEffect
                readyMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), MapConfig.DEFAULT_ZOOM)
                )
            }

            SpotsLayer(
                mapView = mapView,
                map = map,
                style = mapStyle,
                spots = spots,
                onSpotClick = { selectedSpot = it }
            )

            DownloadStatusBanner(
                state = downloadState,
                onDismiss = { viewModel.dismissDownloadState() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            if (regions.isNotEmpty()) {
                SavedRegionsPanel(
                    regions = regions,
                    onDelete = { viewModel.deleteRegion(it) },
                    onOpen = { region ->
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(region.toBounds(), 32)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // Справа место под плавающие кнопки, иначе список уезжает под них.
                        .padding(start = 16.dp, end = 88.dp, bottom = 16.dp)
                )
            }
        }
    }

    if (showSaveDialog) {
        val readyMap = map
        SaveRegionDialog(
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

    newSpotPoint?.let { point ->
        AddSpotDialog(
            point = point,
            fishList = fishList,
            onDismiss = { newSpotPoint = null },
            onConfirm = { name, fishId, note ->
                viewModel.addSpot(
                    name = name,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    fishId = fishId,
                    note = note
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
    onConfirm: (name: String, fishId: Int?, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var fishId by remember { mutableStateOf<Int?>(null) }

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
                onClick = { onConfirm(name.ifBlank { "Точка" }, fishId, note) }
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

private fun MapRegionEntity.toBounds(): LatLngBounds =
    LatLngBounds.Builder()
        .include(LatLng(north, east))
        .include(LatLng(south, west))
        .build()

@Composable
private fun SaveRegionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранить область") },
        text = {
            Column {
                Text("Скачается то, что сейчас видно на экране, вместе с более крупными масштабами.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "Без названия" }) }
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

@Composable
private fun SavedRegionsPanel(
    regions: List<MapRegionEntity>,
    onDelete: (MapRegionEntity) -> Unit,
    onOpen: (MapRegionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Офлайн-области",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 160.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(regions) { region ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            TextButton(onClick = { onOpen(region) }) {
                                Text(region.name)
                            }
                            if (region.sizeBytes > 0) {
                                Text(
                                    text = "${region.sizeBytes / 1024} КБ",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        IconButton(onClick = { onDelete(region) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить область")
                        }
                    }
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
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}
