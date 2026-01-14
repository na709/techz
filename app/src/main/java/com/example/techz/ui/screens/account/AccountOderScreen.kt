package com.example.techz.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.techz.model.Order // Import model Order thật
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import com.example.techz.ui.components.TechZBottomBar
import com.example.techz.ui.navigation.Screen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOrderScreen(
    navController: NavHostController
) {
    val brandColor = Color(0xFF00A9FF)
    val userName = UserSession.currentUserName



    /*var orderList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }*/



    var orderList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        val userId = UserSession.currentUserId
        if (userId != null) {
            RetrofitClient.instance.getUserOrders(userId).enqueue(object : Callback<List<Order>> {
                override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        orderList = response.body() ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                    isLoading = false
                }
            })
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = brandColor)
            }
        } else if (orderList.isEmpty()) {
            EmptyOrderState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orderList) { order ->
                    OrderItem(order, navController = navController)
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, navController: NavController) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Xử lý màu trạng thái
    val statusColor = when (order.status) {

        "Đã Thanh Toán","Đã Giao" -> Color(0xFF4CAF50)
        "Đã Hủy" -> Color(0xFFF44336)
        //"Chờ Xác Nhận" -> Color(0xFFFF9800)
        "Đang Giao", "Đang Vận Chuyển" -> Color(0xFF2196F3)
        else -> Color(0xFFFF9800)
    }

    // Xử lý tên sản phẩm hiển thị
    val displayProductName = if (order.totalItems > 1) {
        "${order.firstProductName} và ${order.totalItems - 1} sản phẩm khác"
    } else {
        order.firstProductName ?: "Sản phẩm TechZ"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Đơn hàng #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = order.status, color = statusColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }

            Text(text = order.date, fontSize = 12.sp, color = Color.Gray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = order.firstProductImage,
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
                        text = displayProductName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "Tổng số lượng: ${order.totalItems}", fontSize = 13.sp, color = Color.Gray)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                thickness = 0.5.dp,
                color = Color.LightGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.OrderDetail.route+"/${order.id}")
                    },
                    modifier = Modifier
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF03A9F4)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Xem chi tiết",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Thành tiền: ${formatter.format(order.total)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF03A9F4)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyOrderState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Bạn chưa có đơn hàng nào", fontSize = 16.sp, color = Color.Gray)
    }
}