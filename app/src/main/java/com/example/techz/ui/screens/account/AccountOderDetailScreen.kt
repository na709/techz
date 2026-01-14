package com.example.techz.ui.screens.account

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.techz.model.OrderDetailResponse
import com.example.techz.service.RetrofitClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*


// --- 2. VIEWMODEL (Xử lý logic tải dữ liệu) ---
// --- 2. VIEWMODEL (Sửa lại hàm fetchOrderDetail) ---
class OrderDetailViewModel : ViewModel() {
    var orderDetail by mutableStateOf<OrderDetailResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchOrderDetail(orderId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getOrderDetail(orderId)
                orderDetail = response
                Log.d("OrderDetail", "Dữ liệu nhận được: ${response.order.id_don_hang}")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("OrderDetail", "Lỗi API: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOrderDetailScreen(
    navController: NavController,
    orderId: Int,
    viewModel: OrderDetailViewModel = viewModel()
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetail(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng #$orderId", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03A9F4))
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF03A9F4))
            }
        } else {
            viewModel.orderDetail?.let { detail ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val statusColor = when (detail.order.trang_thai) {
                                "Đã Thanh Toán","Đã giao" -> Color(0xFF4CAF50)
                                "Chờ Xác Nhận" -> Color(0xFFFF9800)
                                "Đã hủy" -> Color(0xFFFF4433)
                                "Đang giao", "Đang vận chuyển" -> Color(0xFF2196F3)
                                else -> Color.Gray
                            }
                            Text(
                                text = "Trạng thái: ${detail.order.trang_thai}",
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Ngày đặt: ${detail.order.ngay_dat_hang}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Thông tin nhận hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = detail.order.diachi_giao_hang,
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Ghi chú: ${detail.order.ghi_chu}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                    Text("Sản phẩm đã mua", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    detail.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.hinh_anh,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(item.ten_san_pham, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 2)
                                Text("Số lượng: ${item.so_luong}", color = Color.Gray, fontSize = 13.sp)
                            }
                            Text(
                                text = currencyFormat.format(item.thanh_tien),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Thành tiền", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(
                            text = currencyFormat.format(detail.order.tong_tien),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF03A9F4),
                            fontSize = 18.sp
                        )
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy dữ liệu đơn hàng")
            }
        }
    }
}