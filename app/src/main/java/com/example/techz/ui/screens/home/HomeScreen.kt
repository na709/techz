package com.example.techz.ui.screens.home
//import com.example.techz.ui.screens.product.CATEGORIES
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.components.BannerScroll
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import com.example.techz.utils.getCategoryIcon
import com.example.techz.utils.getCategoryName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    var categoryList by remember { mutableStateOf<List<String>>(emptyList()) }
    val bannerList = listOf(
        "https://s3.cloudfly.vn/techz-product-images1/banner/banner-sales.jpg",
        "https://s3.cloudfly.vn/techz-product-images1/banner/banner-sales1.png",
        "https://s3.cloudfly.vn/techz-product-images1/banner/banner-sales2.png",
        "https://s3.cloudfly.vn/techz-product-images1/banner/banner-sales3.png",
        "https://s3.cloudfly.vn/techz-product-images1/banner/banner-sales4.png"
    )

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        currentName = sharedPref.getString("USER_NAME", null)
        val api = RetrofitClient.instance

        // call api get products
        api.getListProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) productList = response.body() ?: emptyList()
                isLoading = false
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) { isLoading = false }
        })
        //call lấy loại linh kiện
        api.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    categoryList = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("HomeScreen", "Lỗi lấy danh mục: ${t.message}")
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
                    containerColor = Color(0xFF00A9FF),
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
                    /*AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("http://160.250.247.5/images/banner-sales.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Banner Sales",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )*/
                    BannerScroll(
                        bannerUrls = bannerList,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    if (categoryList.isNotEmpty()) {
                        PaddingText(text = "Danh mục sản phẩm")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(categoryList) { category ->
                                CategoryItem(category = category, onClick = { onCategoryClick(category) })
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sản phẩm Mới 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onViewAll) {
                            Text("Xem tất cả", color = Color(0xFF00A9FF))
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(productList.take(10)) { product ->
                            ProductItem(product, onProductClick)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PaddingText(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
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

            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = category,
                tint = Color(0xFF00A9FF),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = getCategoryName(category),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            color = Color.Black
        )
    }
}