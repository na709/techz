package com.example.techz.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.techz.ui.components.TechZBottomBarFull
import com.example.techz.ui.navigation.Screen

// --- DATA MODELS ---
enum class OrderStatus(val label: String, val color: Color) {
    PENDING("Chờ xác nhận", Color(0xFFFF9800)),
    SHIPPING("Đang giao", Color(0xFF2196F3)),
    DELIVERED("Đã giao", Color(0xFF4CAF50)),
    CANCELLED("Hủy", Color(0xFFF44336))
}

data class Order(
val id: String,
val name: String,
val date: String,
val time: String,
val totalPrice: String,
val quantity: Int,
val status: OrderStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(navController: NavHostController) {
    // Tạo danh sách dài hơn để test cuộn
    val orders = List(10) { index ->
        when (index % 4) {
            0 -> Order("DH00$index", "MacBook Pro M2", "20/10/2023", "10:30", "32.000.000đ", 1, OrderStatus.SHIPPING)
            1 -> Order("DH00$index", "iPhone 15 Pro", "21/10/2023", "09:15", "34.990.000đ", 2, OrderStatus.PENDING)
            2 -> Order("DH00$index", "Sony WH-1000XM5", "22/10/2023", "14:20", "8.500.000đ", 1, OrderStatus.DELIVERED)
            else -> Order("DH00$index", "Samsung S23", "22/10/2023", "15:00", "22.000.000đ", 1, OrderStatus.CANCELLED)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Đơn Hàng", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = {navController.navigate(Screen.AdminDashboard.route) {

                        popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                        {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03A9F4))
            )
        },
        bottomBar = {  TechZBottomBarFull(navController = navController)
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // --- PHẦN CỐ ĐỊNH (KHÔNG CUỘN) ---

            // 1. Tabs
            val tabs = listOf("Tất cả", "Chờ xác nhận", "Đang giao", "Đã giao", "Hủy")
            var selectedTabIndex by remember { mutableStateOf(0) }

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.size) { index ->
                    FilterTab(
                        text = tabs[index],
                        isSelected = index == selectedTabIndex,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // 2. Header nhỏ
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Danh sách đơn hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
            }

            // --- PHẦN CUỘN ĐƯỢC ---
            // QUAN TRỌNG: Sử dụng weight(1f) để LazyColumn chiếm hết phần còn lại và kích hoạt thanh cuộn
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // <-- Dòng này giúp danh sách cuộn được
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderItem(order)
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {

            // Hàng 1: Thời gian & Trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${order.date} - ${order.time}", fontSize = 13.sp, color = Color.Gray)
                Surface(
                    color = order.status.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status.label,
                        fontSize = 12.sp,
                        color = order.status.color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

            // Hàng 2: Tên & Giá
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text("Mã: ${order.id}", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tổng tiền", fontSize = 12.sp, color = Color.Gray)
                    Text(order.totalPrice, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Số lượng: ${order.quantity}", fontSize = 13.sp, color = Color.Gray)

            // Nút bấm (Logic cũ: Chỉ hiện khi Chờ xác nhận hoặc Đang giao)
            if (order.status == OrderStatus.PENDING || order.status == OrderStatus.SHIPPING) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (order.status == OrderStatus.PENDING) {
                        ActionButton(text = "Từ chối", isPrimary = false, modifier = Modifier.weight(1f))
                        ActionButton(text = "Xác nhận", isPrimary = true, modifier = Modifier.weight(1f))
                    } else if (order.status == OrderStatus.SHIPPING) {
                        ActionButton(text = "Hủy đơn", isPrimary = false, modifier = Modifier.weight(1f))
                        ActionButton(text = "Đã giao", isPrimary = true, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// --- Component phụ ---
@Composable
fun FilterTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF03A9F4) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ActionButton(text: String, isPrimary: Boolean, modifier: Modifier = Modifier) {
    Button(
        onClick = { },
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color(0xFF03A9F4) else Color.White
        ),
        border = if (!isPrimary) BorderStroke(1.dp, Color.Gray) else null,
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text, color = if (isPrimary) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewOrderScroll() {
    val navController = rememberNavController()
    AdminOrderScreen(navController)
}