package com.example.techz.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.techz.ui.navigation.Screen


@Composable
fun TechZBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val brandColor = Color(0xFF0066FF)

    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp, contentColor = brandColor) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0066FF),
                selectedTextColor = Color(0xFF0066FF),
                indicatorColor = Color(0xFFE6F0FF)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Sản phẩm") },
            label = { Text("Sản phẩm") },
            selected = currentRoute == Screen.ProductList.route,
            onClick = {
                navController.navigate(Screen.ProductList.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0066FF),
                selectedTextColor = Color(0xFF0066FF),
                indicatorColor = Color(0xFFE6F0FF)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
            label = { Text("Tài khoản") },
            selected = false, // Chưa làm
            onClick = { /* TODO */ },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Gray)
        )
    }
}