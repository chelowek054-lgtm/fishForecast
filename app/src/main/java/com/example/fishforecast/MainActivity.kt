package com.example.fishforecast

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fishforecast.ui.addeditfish.AddEditFishScreen
import com.example.fishforecast.ui.fishlist.FishListScreen
import com.example.fishforecast.ui.navigation.AddEditFishRoute
import com.example.fishforecast.ui.map.MapScreen
import com.example.fishforecast.ui.navigation.FishListRoute
import com.example.fishforecast.ui.navigation.MapRoute
import com.example.fishforecast.ui.navigation.WeatherRoute
import com.example.fishforecast.ui.theme.FishForecastTheme
import com.example.fishforecast.ui.weather.WeatherScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.KClass

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FishForecastTheme {
                FishForecastAppNavigation()
            }
        }
    }
}

private data class BottomNavItem(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(FishListRoute, FishListRoute::class, "Рыбы", Icons.AutoMirrored.Filled.List),
    BottomNavItem(WeatherRoute, WeatherRoute::class, "Погода", Icons.Default.Cloud),
    BottomNavItem(MapRoute, MapRoute::class, "Карта", Icons.Default.Map)
)

@Composable
fun FishForecastAppNavigation() {
    val navController = rememberNavController()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.hasRoute(item.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FishListRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<FishListRoute> {
                FishListScreen(
                    onAddFish = {
                        navController.navigate(AddEditFishRoute())
                    },
                    onEditFish = { fishId ->
                        navController.navigate(AddEditFishRoute(fishId))
                    }
                )
            }
            composable<WeatherRoute> {
                WeatherScreen()
            }
            composable<MapRoute> {
                MapScreen()
            }
            composable<AddEditFishRoute> {
                AddEditFishScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
