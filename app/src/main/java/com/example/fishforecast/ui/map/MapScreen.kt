package com.example.fishforecast.ui.map

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/** Стиль тайлов по умолчанию. Заменяется на офлайн-источник в шаге 2. */
private const val MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"

private const val DEFAULT_ZOOM = 12.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val userLocation by viewModel.userLocation

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Карта") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.locateUser() }) {
                Icon(Icons.Default.LocationOn, contentDescription = "Моё местоположение")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val mapView = rememberMapViewWithLifecycle()
            var map by remember { mutableStateOf<MapLibreMap?>(null) }

            AndroidView(
                factory = {
                    mapView.getMapAsync { readyMap ->
                        readyMap.setStyle(MAP_STYLE_URL)
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
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), DEFAULT_ZOOM)
                )
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
        MapLibre.getInstance(context)
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
