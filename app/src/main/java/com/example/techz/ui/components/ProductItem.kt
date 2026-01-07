package com.example.techz.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.techz.model.Product
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductItem(product: Product, onClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
            .clickable { onClick(product.id) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.image)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    minLines = 2
                )

                Spacer(Modifier.height(4.dp))

                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(product.price)
                Text(
                    text = formattedPrice,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { onClick(product.id) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xem chi tiết", fontSize = 12.sp)
                }
            }
        }
    }
}