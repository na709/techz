package com.example.techz.ui.screens.product

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.components.ProductItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onAddToCart: () -> Unit,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var relatedProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(productId) {
        isLoading = true

        RetrofitClient.instance.getProductDetail(productId).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val currentProduct = response.body()
                    product = currentProduct

                    if (currentProduct != null) {
                        RetrofitClient.instance.getListProducts().enqueue(object : Callback<List<Product>> {
                            override fun onResponse(call: Call<List<Product>>, res: Response<List<Product>>) {
                                if (res.isSuccessful) {
                                    val allProducts = res.body() ?: emptyList()

                                    relatedProducts = allProducts.filter { item ->
                                        item.id != currentProduct.id && item.category == currentProduct.category
                                    }
                                        .sortedBy { abs(it.price - currentProduct.price) }
                                        .take(4)
                                }
                            }
                            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                            }
                        })
                    }
                }
                isLoading = false
            }
            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.e("ProductDetail", "Error: ${t.message}")
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        product?.name ?: "Chi tiết sản phẩm",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (product != null) {
                Surface(shadowElevation = 16.dp) {
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A9FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("THÊM VÀO GIỎ HÀNG", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (product == null) {
                Text("Không tìm thấy sản phẩm!", modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFFF5F5F5))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product!!.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = product!!.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.White),
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        Modifier
                            .padding(top = 8.dp)
                            .background(Color.White)
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = product!!.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(product!!.price)
                        Text(
                            text = formattedPrice,
                            fontSize = 24.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Column(
                        Modifier
                            .background(Color.White)
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Mô tả sản phẩm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = product!!.description ?: "Đang cập nhật...",
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (relatedProducts.isNotEmpty()) {
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            Text(
                                text = "Sản phẩm liên quan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                            val rows = relatedProducts.chunked(2)
                            rows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (item in rowItems) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            ProductItem(product = item, onClick = onProductClick)
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}