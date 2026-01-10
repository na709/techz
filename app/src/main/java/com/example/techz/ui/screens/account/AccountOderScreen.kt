package com.example.techz.ui.screens.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.techz.service.UserSession
import com.example.techz.ui.components.TechZBottomBar
import java.text.NumberFormat
import java.util.Locale

// --- 1. MODEL GIẢ LẬP (Để test giao diện) ---
data class OrderUI(
    val id: String,
    val date: String,
    val status: String, // "Chờ xác nhận", "Đang giao", "Hoàn thành", "Đã hủy"
    val totalAmount: Double,
    val productName: String,
    val productImageUrl: String,
    val quantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOrderScreen(
    navController: NavHostController
) {
    val brandColor = Color(0xFF00A9FF)
    val userName = UserSession.currentUserName

    // Dữ liệu giả (Sau này thay bằng gọi API)
    val sampleOrders = listOf(
        OrderUI("#ORD-001", "20/10/2025", "Hoàn thành", 15000000.0, "Card màn hình RTX 3060", "https://product.hstatic.net/200000722513/product/thumb_vga_rtx_3060_db19b16e4928424a872322313620958c_grande.jpg", 1),
        OrderUI("#ORD-002", "22/10/2025", "Đang giao", 2500000.0, "Ram Corsair 16GB", "https://product.hstatic.net/1000026716/product/cmw16gx4m2c3200c16_gallery_01_d6006f857e4e4277a06488d052a65824_master.jpg", 2),
        OrderUI("#ORD-003", "15/09/2025", "Đã hủy", 500000.0, "Chuột Logitech G102", "https://product.hstatic.net/1000026716/product/logitech-g102-gen2-lightsync-white-1_248b945b63004d0c95027418a0a9e708_master.jpg", 1)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = brandColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = { TechZBottomBar(navController, userName) },
        containerColor = Color(0xFFF5F5F5) // Màu nền xám nhẹ cho nổi bật Card đơn hàng
    ) { innerPadding ->

        if (sampleOrders.isEmpty()) {
            // --- GIAO DIỆN KHI TRỐNG ---
            EmptyOrderState(modifier = Modifier.padding(innerPadding))
        } else {
            // --- DANH SÁCH ĐƠN HÀNG ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách giữa các đơn
            ) {
                items(sampleOrders) { order ->
                    OrderItem(order)
                }
            }
        }
    }
}

// --- COMPOSABLE: MỘT THẺ ĐƠN HÀNG ---
@Composable
fun OrderItem(order: OrderUI) {
    // Hàm định dạng tiền tệ
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Màu sắc trạng thái
    val statusColor = when (order.status) {
        "Hoàn thành" -> Color(0xFF4CAF50) // Xanh lá
        "Đã hủy" -> Color(0xFFF44336)     // Đỏ
        "Đang giao" -> Color(0xFF2196F3)  // Xanh dương
        else -> Color(0xFFFF9800)         // Cam (Chờ xác nhận)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Header: Mã đơn + Trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = order.status,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Text(text = order.date, fontSize = 12.sp, color = Color.Gray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            // 2. Body: Ảnh + Tên sản phẩm
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = order.productImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = order.productName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "x${order.quantity}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            // 3. Footer: Tổng tiền
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thành tiền: ", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = formatter.format(order.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00A9FF) // Màu xanh thương hiệu
                )
            }
        }
    }
}

// --- COMPOSABLE: TRẠNG THÁI TRỐNG ---
@Composable
fun EmptyOrderState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bạn chưa có đơn hàng nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}