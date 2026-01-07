package com.example.techz.ui.screens.product


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: Int, onAddToCart: () -> Unit, onBack: () -> Unit) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(productId) {
        RetrofitClient.instance.getProductDetail(productId).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    product = response.body()
                }
                isLoading = false
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.e("ProductDetail", "Lỗi: ${t.message}")
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
                Surface(

                    shadowElevation = 8.dp
                ) {
                    PaddingValues(16.dp)
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = MaterialTheme.shapes.medium
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
                Text(
                    text = "Không tìm thấy sản phẩm!",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
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

                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = product!!.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(product!!.price)
                        Text(
                            text = formattedPrice,
                            fontSize = 22.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Mô tả sản phẩm:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = product!!.description ?: "Đang cập nhật...",
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}