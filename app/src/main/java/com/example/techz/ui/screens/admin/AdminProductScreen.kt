package com.example.techz.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

data class Product(val id: Int, val name: String, val price: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductListScreen(
    navController: NavHostController,
    onEditProduct: (Int) -> Unit = {},
    onDeleteProduct: (Int) -> Unit = {}
) {
    // 4 sản phẩm
    val productList = List(4) {
        Product(it, "Intel Core i9-13900K", "12.990.000đ")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TechZ Store", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03A9F4))
            )
        },
        bottomBar = { AdminBottomBarUI() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = Color(0xFF03A9F4),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {

            // --- PHẦN 1: HEADER & TÌM KIẾM ---
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Tìm kiếm sản phẩm...", fontSize = 14.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF03A9F4)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Danh sách sản phẩm", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("${productList.size} hiển thị", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- PHẦN 2: LƯỚI SẢN PHẨM (Chiếm phần lớn không gian) ---
            // Sử dụng weight(1f) để đẩy thanh phân trang xuống dưới cùng
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f) // QUAN TRỌNG: Chiếm hết khoảng trống còn lại
                    .fillMaxWidth()
            ) {
                items(productList) { product ->
                    ProductItemStandard(
                        product = product,
                        onEditClick = { onEditProduct(product.id) },
                        onDeleteClick = { onDeleteProduct(product.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- PHẦN 3: PHÂN TRANG (Cố định ở đáy layout, không cuộn cùng grid) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Giảm padding card để gọn hơn
                    horizontalArrangement = Arrangement.Center, // Căn giữa nội dung phân trang
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.Gray, modifier = Modifier.clickable {})
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("1", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("...", fontSize = 18.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(20.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Black, modifier = Modifier.clickable {})
                }
            }

            // Spacer nhỏ để tránh sát đáy màn hình quá
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProductItemStandard(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh sản phẩm: Chỉnh tỉ lệ 1.3f để cân đối hơn, không quá cao
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Text("IMG", color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tên
            Text(
                text = product.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1, // Giới hạn 1 dòng để gọn chiều cao
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Giá
            Text(
                text = product.price,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nút bấm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFE3F2FD), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFFEBEE), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AdminBottomBarUI() {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") },
            selected = false, onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Product") },
            selected = true, onClick = { },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF03A9F4), indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Account") },
            selected = false, onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Dash") },
            selected = false, onClick = { }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewLayout() {
    val navController = rememberNavController()
    AdminProductListScreen(navController)
}