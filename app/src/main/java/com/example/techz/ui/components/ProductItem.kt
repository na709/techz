package com.example.techz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techz.model.Product

@Composable
fun ProductItem(product: Product, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick(product.id) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(120.dp).background(product.imageColor.copy(alpha=0.3f)))
            Column(Modifier.padding(8.dp)) {
                Text(product.name, maxLines = 2, fontSize = 14.sp, fontWeight = FontWeight.Bold, minLines = 2)
                Text(product.price, color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onClick(product.id) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                ) {
                    Text("Xem ngay", fontSize = 12.sp)
                }
            }
        }
    }
}