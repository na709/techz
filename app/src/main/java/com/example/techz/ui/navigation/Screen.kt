package com.example.techz.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Account : Screen("account")
    object ProductList : Screen("product_list")
    object Cart : Screen("cart")
    object Payment : Screen("payment")
    object Detail : Screen("detail/{id}") {
        fun passId(id: String) = "detail/$id"
    }
}