package com.example.techz.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.techz.utils.fakeProducts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(onCheckout: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Giỏ hàng") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            LazyColumn(Modifier.weight(1f)) {
                items(fakeProducts.take(2)) { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(60.dp).background(item.imageColor)); Spacer(Modifier.width(16.dp))
                        Column { Text(item.name); Text(item.price, color = Color.Red) }
                    }
                    Divider()
                }
            }
            Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))) { Text("THANH TOÁN") }
        }
    }
}