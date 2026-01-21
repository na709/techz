package com.example.techz.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.model.AuthResponse
import com.example.techz.model.OrderActionRequest
import com.example.techz.model.OrderResponse
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession // Import UserSession
import com.example.techz.ui.components.TechZBottomBarFull
import com.example.techz.ui.navigation.Screen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

// --- 1. CÁC HÀM TIỆN ÍCH ---
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "chờ xác nhận" -> Color(0xFFFF9800)
        "đang vận chuyển", "đang giao" -> Color(0xFF2196F3)
        "đã giao" -> Color(0xFF4CAF50)
        "đã hủy", "hủy" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

fun formatPrice(price: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(price).replace(",", ".") + " đ"
}

// --- 2. MÀN HÌNH CHÍNH ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(navController: NavHostController) {
    val context = LocalContext.current

    // --- THAY ĐỔI 1: LẤY ID TỪ USERSESSION ---
    // Không dùng SharedPreferences "AppPrefs" nữa
    val currentAdminId = UserSession.currentUserId ?: -1

    var orderList by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val tabs = listOf("Tất cả", "Chờ Xác Nhận", "Đang Vận Chuyển", "Đã Giao", "Đã Hủy")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    fun processOrderAction(orderId: Int, action: String) {
        if (currentAdminId == -1) {
            Toast.makeText(context, "Lỗi: Phiên đăng nhập hết hạn!", Toast.LENGTH_LONG).show()
            return
        }

        val request = OrderActionRequest(orderId = orderId)
        val apiCall = when (action) {
            "CONFIRM" -> RetrofitClient.instance.confirmOrder(currentAdminId, request)
            "CANCEL" -> RetrofitClient.instance.cancelOrder(currentAdminId, request)
            "DELIVERED" -> RetrofitClient.instance.deliveredOrder(currentAdminId, request)
            else -> return
        }

        apiCall.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val nextStatus = when (action) {
                        "CONFIRM" -> "Đang Vận Chuyển"
                        "CANCEL" -> "Đã Hủy"
                        "DELIVERED" -> "Đã Giao"
                        else -> ""
                    }

                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()

                    // Cập nhật danh sách tại chỗ (Optimistic Update)
                    orderList = orderList.map {
                        if (it.id == orderId) {
                            it.copy(
                                status = nextStatus,
                                paymentStatus = if (action == "DELIVERED") "Đã thanh toán" else it.paymentStatus
                            )
                        } else it
                    }
                } else {
                    Toast.makeText(context, "Thất bại: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    LaunchedEffect(Unit) {
        // Đảm bảo Session được load nếu lỡ user reload app ở màn hình này
        UserSession.initSession(context)

        // Lấy lại ID sau khi init (đề phòng trường hợp biến RAM bị null)
        val idToCheck = UserSession.currentUserId ?: -1

        if (idToCheck == -1) {
            isLoading = false
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Login.route)
            return@LaunchedEffect
        }

        RetrofitClient.instance.getAllOrders(idToCheck).enqueue(object : Callback<List<OrderResponse>> {
            override fun onResponse(call: Call<List<OrderResponse>>, response: Response<List<OrderResponse>>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    orderList = response.body()!!.reversed()
                }
            }
            override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                isLoading = false
            }
        })
    }

    val filteredList = if (selectedTabIndex == 0) orderList else {
        val key = tabs[selectedTabIndex]
        orderList.filter {
            val s = it.status.lowercase()
            if (key == "Đang Vận Chuyển") s == "đang vận chuyển" || s == "đang giao"
            else if (key == "Đã Hủy") s == "đã hủy" || s == "hủy"
            else s == key.lowercase()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Đơn Hàng", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.AdminDashboard.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03A9F4))
            )
        },
        bottomBar = { TechZBottomBarFull(navController = navController) },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.size) { index ->
                    FilterTab(text = tabs[index], isSelected = index == selectedTabIndex, onClick = { selectedTabIndex = index })
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { order ->
                        OrderItem(
                            order = order,
                            onAction = { action -> processOrderAction(order.id, action) },
                            onClick = { id ->
                                navController.navigate("admin_order_detail/$id")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: OrderResponse, onAction: (String) -> Unit,onClick: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick(order.id)}
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = order.date, fontSize = 13.sp, color = Color.Gray)
                Surface(color = getStatusColor(order.status).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(text = order.status, fontSize = 12.sp, color = getStatusColor(order.status), fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp, 4.dp))
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
            Text(text = order.productName ?: "Đơn hàng #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            // 1. Hiển thị Tổng tiền
            Text(text = "Tổng tiền: ${formatPrice(order.totalPrice)}", color = Color.Red, fontWeight = FontWeight.Bold)

            // 2. Hiển thị Trạng thái thanh toán
            val isPaid = order.paymentStatus == "Đã thanh toán"
            Column {
                Text(
                    text = "Thanh toán: ${order.paymentStatus ?: "Chưa thanh toán"}",
                    color = if (isPaid) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(text = "Số lượng: ${order.quantity}", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Giữ nguyên các nút chức năng cũ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (order.status.lowercase()) {
                    "chờ xác nhận" -> {
                        ActionButton("Từ chối", false, Modifier.weight(1f)) { onAction("CANCEL") }
                        ActionButton("Duyệt đơn", true, Modifier.weight(1f)) { onAction("CONFIRM") }
                    }
                    "đang vận chuyển", "đang giao" -> {
                        ActionButton("Xác nhận đã giao", true, Modifier.weight(1f)) { onAction("DELIVERED") }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, isPrimary: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isPrimary) Color(0xFF03A9F4) else Color.White),
        border = if (!isPrimary) BorderStroke(1.dp, Color.Gray) else null
    ) {
        Text(text, color = if (isPrimary) Color.White else Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FilterTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF03A9F4) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}