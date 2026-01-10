package com.example.techz.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.ui.navigation.Screen

@Composable
fun TechZBottomBarFull(navController: NavHostController) {
    NavigationBar(
        containerColor = Color(0xFFEEEEEE),
        tonalElevation = 8.dp
    ) {
        // --- 1. NÚT TRANG CHỦ (HOME) ---
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = false,
            onClick = {
                // Chuyển sang màn hình Home
                navController.navigate(Screen.Home.route) {
                    // Xóa backstack để tránh bị chồng màn hình
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // --- 2. NÚT SẢN PHẨM (PRODUCT LIST) ---
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Product") },
            label = { Text("Sản phẩm", fontSize = 10.sp) },
            selected = false,
            onClick = {
                // Chuyển sang màn hình ProductListScreen
                navController.navigate(Screen.ProductList.route) {
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // --- 3. NÚT TÀI KHOẢN (ACCOUNT) ---
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Account") },
            label = { Text("Tài khoản", fontSize = 10.sp) },
            selected = false,
            onClick = {
                // Nếu bạn có màn hình Account thì thêm vào đây:
                // navController.navigate(Screen.Account.route)
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // --- 4. NÚT DASHBOARD (ADMIN - ĐANG CHỌN) ---
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = true, // Vì đang ở Dashboard nên để true
            onClick = {
                // Đang ở đây rồi thì không làm gì hoặc load lại
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF03A9F4),
                selectedTextColor = Color(0xFF03A9F4),
                indicatorColor = Color.White
            )
        )
    }
}