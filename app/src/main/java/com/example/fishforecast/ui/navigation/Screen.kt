package com.example.fishforecast.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object FishListRoute

@Serializable
object WeatherRoute

@Serializable
data class AddEditFishRoute(val fishId: Int = NEW_FISH_ID) {
    companion object {
        const val NEW_FISH_ID = -1
    }
}
