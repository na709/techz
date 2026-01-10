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
        containerColor = Color(0xFFEEEEEE), // Màu nền xám nhạt cho thanh bar
        tonalElevation = 8.dp
    ) {
        // Item 1: Trang chủ
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = false,
            onClick = { navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
                      },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        // Item 2: Sản phẩm
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Product") },
            label = { Text("Sản phẩm", fontSize = 10.sp) },
            selected = false,
            onClick = { navController.navigate(Screen.ProductList.route) {
                popUpTo(Screen.Home.route)
                launchSingleTop = true
            }
                      },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        // Item 3: Tài khoản
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Account") },
            label = { Text("Tài khoản", fontSize = 10.sp) },
            selected = false,
            onClick = { /* Nav to Account */ },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        // Item 4: DASHBOARD (ĐANG ĐƯỢC CHỌN - MÀU XANH)
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Dashboard") }, // Icon bánh răng/Dashboard
            label = { Text("Dashboard", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = true, // Đang ở màn hình này nên set true
            onClick = { /* Do nothing */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF03A9F4), // Màu icon khi chọn (Xanh)
                selectedTextColor = Color(0xFF03A9F4), // Màu chữ khi chọn
                indicatorColor = Color.White // Màu nền tròn sau icon
            )
        )
    }
}