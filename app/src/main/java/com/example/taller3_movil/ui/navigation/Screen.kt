package com.example.taller3_movil.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Map : Screen("map")
    object Profile : Screen("profile")
}
