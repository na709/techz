package com.example.techz.ui.screens.product


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techz.utils.fakeProducts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String?, onAddToCart: () -> Unit, onBack: () -> Unit) {
    val product = fakeProducts.find { it.id == productId } ?: fakeProducts.first()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(250.dp).background(product.imageColor.copy(alpha=0.3f)))
            Column(Modifier.padding(16.dp)) {
                Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(product.price, fontSize = 20.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Mô tả: Hàng chính hãng, bảo hành 36 tháng. Hỗ trợ lắp đặt tại nhà.", color = Color.Gray)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                ) { Text("THÊM VÀO GIỎ HÀNG") }
            }
        }
    }
}