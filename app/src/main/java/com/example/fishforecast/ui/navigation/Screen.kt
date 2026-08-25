package com.example.fishforecast.ui.navigation

sealed class Screen(val route: String) {
    object FishList : Screen("fish_list")
    object Weather : Screen("weather")
    object AddEditFish : Screen("add_edit_fish?fishId={fishId}") {
        fun passId(id: Int? = null): String {
            return "add_edit_fish?fishId=${id ?: -1}"
        }
    }
}