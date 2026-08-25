package com.example.fishforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fishforecast.ui.addeditfish.AddEditFishScreen
import com.example.fishforecast.ui.fishlist.FishListScreen
import com.example.fishforecast.ui.navigation.Screen
import com.example.fishforecast.ui.theme.FishForecastTheme
import dagger.hilt.android.AndroidEntryPoint

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

@Composable
fun FishForecastAppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.FishList.route
    ) {
        composable(route = Screen.FishList.route) {
            FishListScreen(
                onAddFish = {
                    navController.navigate(Screen.AddEditFish.passId())
                },
                onEditFish = { fishId ->
                    navController.navigate(Screen.AddEditFish.passId(fishId))
                }
            )
        }
        composable(
            route = Screen.AddEditFish.route,
            arguments = listOf(
                navArgument("fishId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            AddEditFishScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}