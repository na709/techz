package com.example.techz.ui.screens.admin

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.techz.ui.components.TechZBottomBarFull
import com.example.techz.ui.navigation.Screen

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
        DashboardItem("Order", Icons.Outlined.ShoppingCart) { navController.navigate(Screen.AdminOrder.route) },
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
        bottomBar = {  TechZBottomBarFull(navController = navController)
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


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenPreview() {
    val navController = rememberNavController()
    AdminDashboardScreen(navController = navController, onLogout = { })
}