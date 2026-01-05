package com.example.techz.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import com.example.techz.utils.fakeProducts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavHostController, onProductClick: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tất cả linh kiện") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0066FF), titleContentColor = Color.White)
            )
        },
        bottomBar = { TechZBottomBar(navController) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(fakeProducts) { product -> ProductItem(product, onProductClick) }
        }
    }
}