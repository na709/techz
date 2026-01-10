package com.example.techz.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chi Tiết Đơn Hàng",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 22.sp // Tăng size tiêu đề
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03A9F4))
            )
        },
        bottomBar = { AdminBottomBarUI() }
    ) { padding ->

        // Phần nội dung chính
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Card màu xám chứa thông tin
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Chiếm toàn bộ chiều cao còn lại
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp) // Padding dày hơn chút
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween // Đẩy 2 đầu
                ) {
                    // 1. Phần thông tin chi tiết (Chiếm phần lớn không gian)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceEvenly // QUAN TRỌNG: Tự động chia đều khoảng cách để lấp đầy
                    ) {
                        InfoRowBig(label = "Tên", value = "Nguyễn Văn A")
                        DashedDivider()
                        InfoRowBig(label = "Địa chỉ", value = "123 Đường ABC, Quận 1, TP.HCM")
                        DashedDivider()
                        InfoRowBig(label = "SĐT", value = "0909 123 456")
                        DashedDivider()
                        InfoRowBig(label = "Ghi chú", value = "Giao hành chính")
                        DashedDivider()
                        InfoRowBig(label = "Số lượng", value = "2 sản phẩm")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Phần Tổng tiền (Nằm dưới cùng, chữ rất to)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(thickness = 2.dp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Tổng tiền thanh toán",
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "10.000.000đ", // 10M
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp, // SIZE CỰC ĐẠI
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

// Component hiển thị dòng thông tin SIZE TO
@Composable
fun InfoRowBig(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top // Căn lề trên phòng trường hợp text dài xuống dòng
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp, // Chữ to
            color = Color.Black,
            modifier = Modifier.width(110.dp) // Tăng chiều rộng nhãn để không bị chèn
        )
        Text(
            text = value,
            fontSize = 22.sp, // Chữ to
            color = Color(0xFF333333),
            lineHeight = 30.sp, // Giãn dòng để dễ đọc nếu xuống dòng
            modifier = Modifier.weight(1f)
        )
    }
}

// Đường kẻ đứt nét trang trí (giống hóa đơn)
@Composable
fun DashedDivider() {
    Text(
        text = "- - - - - - - - - - - - - - - - - - - - - - - - -",
        color = Color.Gray,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = Modifier.fillMaxWidth().alpha(0.5f)
    )
}




@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewOrderDetailBig() {
    val navController = rememberNavController()
    OrderDetailScreen(navController)
}