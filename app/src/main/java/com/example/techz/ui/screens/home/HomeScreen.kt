package com.example.techz.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import com.example.techz.utils.fakeProducts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onProductClick: (String) -> Unit,
    onGoToCart: () -> Unit,
    onViewAll: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PC Parts Store", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onGoToCart) { Icon(Icons.Default.ShoppingCart, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0066FF), titleContentColor = Color.White, actionIconContentColor = Color.White)
            )
        },
        bottomBar = { TechZBottomBar(navController) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(150.dp).background(Color(0xFF0066FF), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text("SIÊU SALE 30%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sản phẩm HOT 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onViewAll) { Text("Xem tất cả") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(fakeProducts.take(4)) { product -> ProductItem(product, onProductClick) }
                }
            }
        }
    }
}