package com.example.techz.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// Data model cho các nút chức năng
data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    // Danh sách 5 chức năng quản lý
    val dashboardItems = listOf(
        DashboardItem("Product", Icons.Outlined.Inventory2) { /* Navigate to Product */ },
        DashboardItem("Order", Icons.Outlined.ShoppingCart) { /* Navigate to Order */ },
        DashboardItem("Comment", Icons.AutoMirrored.Outlined.Chat) { /* Navigate to Comment */ },
        DashboardItem("Account", Icons.Outlined.Person) { /* Navigate to Account */ },
        DashboardItem("Voucher", Icons.Outlined.ConfirmationNumber) { /* Navigate to Voucher */ }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TechZ Store",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF03A9F4), // Xanh dương
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        // GỌI COMPONENT BOTTOM BAR TẠI ĐÂY
        bottomBar = {
            TechZBottomBarFull(navController = navController)
        },
        containerColor = Color.White
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. TIÊU ĐỀ
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text(
                        text = "Dashboard",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Manager",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = Color.Black
                    )
                }
            }

            // 2. DANH SÁCH MENU (Product, Order,...)
            items(dashboardItems) { item ->
                DashboardItemCard(
                    title = item.title,
                    icon = item.icon,
                    onClick = item.onClick
                )
            }
            // Spacer cuối để không bị BottomBar che
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// -----------------------------------------------------------
// PHẦN BẠN KHOANH TRÒN: BOTTOM NAVIGATION BAR
// -----------------------------------------------------------
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
            onClick = { /* Nav to Home */ },
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
            onClick = { /* Nav to Product List */ },
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

// -----------------------------------------------------------
// CÁC COMPOSABLE CON KHÁC
// -----------------------------------------------------------

@Composable
fun DashboardItemCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.25f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Icon(icon, contentDescription = title, modifier = Modifier.size(48.dp), tint = Color.Black)
        }
    }
}

@Composable
fun StatCardRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Card con ví dụ
        Card(
            modifier = Modifier.weight(1f).height(80.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Revenue", fontSize = 12.sp, color = Color.Gray)
                Text("50.5M", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Card(
            modifier = Modifier.weight(1f).height(80.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Orders", fontSize = 12.sp, color = Color.Gray)
                Text("1,204", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenPreview() {
    val navController = rememberNavController()
    AdminDashboardScreen(navController = navController, onLogout = { })
}