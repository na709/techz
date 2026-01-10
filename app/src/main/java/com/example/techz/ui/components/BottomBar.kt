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
import com.example.techz.service.UserSession


@Composable
fun TechZBottomBar(
    navController: NavController,
    currentName: String?
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val brandColor = Color(0xFF00A9FF)
    val lightBrandColor = Color(0xFFE6F5FF)
    val userName = UserSession.currentUserName
    val isLoggedIn = UserSession.isLoggedIn
    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = brandColor,
        selectedTextColor = brandColor,
        indicatorColor = lightBrandColor,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
    )

    val accountLabel = if (!userName.isNullOrBlank()) {
        val shortName = userName.trim().substringAfterLast(" ")
        "Hi, $shortName!!"
    } else {
        "Tài khoản"
    }

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
            colors = navigationItemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Sản phẩm") },
            label = { Text("Sản phẩm") },
            selected = currentRoute?.startsWith(Screen.ProductList.route) == true,
            onClick = {
                navController.navigate(Screen.ProductList.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            },
            colors = navigationItemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
            label = { Text(text = accountLabel) },
            selected = currentRoute == Screen.Account.route,
            onClick = {

                navController.navigate(Screen.Account.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            },
            colors = navigationItemColors
        )
    }
}
