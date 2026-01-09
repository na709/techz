package com.example.techz.ui.screens.home
import com.example.techz.ui.screens.product.CATEGORIES
import android.util.Log
<<<<<<< HEAD
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
=======
import android.content.Context
>>>>>>> 019325fb89aa31024a014cfd4a714f62e972271e
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
@Composable//new
fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Laptop", "PC" -> Icons.Default.Computer
        "Chuot", "BanPhim" -> Icons.Default.Keyboard
        "TaiNghe" -> Icons.Default.Headphones
        else -> Icons.Default.Devices
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onProductClick: (Int) -> Unit,
    onGoToCart: () -> Unit,
    onViewAll: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val context = LocalContext.current
    var currentName by remember { mutableStateOf<String?>(null) }
    var productList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        currentName = sharedPref.getString("USER_NAME", null)

        RetrofitClient.instance.getListProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    productList = response.body() ?: emptyList()
                }
                isLoading = false
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("HomeScreen", "Lỗi API: ${t.message}")
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Techz Store", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onGoToCart) {
                        Icon(Icons.Default.ShoppingCart, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0066FF),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = { TechZBottomBar(navController,currentName) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                item {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("http://160.250.247.5/images/banner-sales.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Banner Sales",
                        modifier = Modifier
<<<<<<< HEAD
                            .fillMaxWidth(1f)
=======
                            .fillMaxWidth()
>>>>>>> 019325fb89aa31024a014cfd4a714f62e972271e
                            .height(400.dp)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    Text("Danh mục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(CATEGORIES) { category ->
                            CategoryItem(category = category, onClick = { onCategoryClick(category) })
                        }
                    }
                }

                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sản phẩm Mới 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onViewAll) { Text("Xem tất cả") }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp) // Thêm padding dưới để không bị cắt bóng đổ
                    ) {
                        items(productList.take(5)) { product ->
                            ProductItem(product, onProductClick)
                        }
                    }
                }
            }
        }
    }
}
@Composable//new
fun CategoryItem(category: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE3F2FD)), // Màu nền nhẹ
            contentAlignment = Alignment.Center
        ) {
            // Ở đây dùng Icon tạm, nếu có ảnh từ API thì dùng AsyncImage
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = category,
                tint = Color(0xFF0066FF),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = category,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            color = Color.Black
        )
    }
}