package com.example.techz.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.navigation.Screen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale


class AdminProductViewModel : ViewModel() {
    var products = mutableStateOf<List<Product>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        isLoading.value = true
        errorMessage.value = null

        RetrofitClient.instance.getListProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                isLoading.value = false
                if (response.isSuccessful) {
                    products.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Lỗi: ${response.code()} - ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                isLoading.value = false
                errorMessage.value = "Lỗi kết nối: ${t.message}"
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductScreen(
    navController: NavHostController,
    viewModel: AdminProductViewModel = viewModel()
) {
    val productList = viewModel.products.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.errorMessage.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý sản phẩm (${productList.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.AdminDashboard.route) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF03A9F4))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Text(text = error, color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.loadProducts() }) { Text("Thử lại") }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productList) { product ->
                        AdminProductItem(
                            product = product,
                            onEditClick = { productId ->
                                // Ví dụ: Điều hướng sang màn hình sửa (cần khai báo route này sau)
                                // navController.navigate("admin_product_edit/$productId")
                                println("Click sửa ID: $productId")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductItem(
    product: Product,
    onEditClick: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = product.image,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    loading = { CircularProgressIndicator(modifier = Modifier.scale(0.5f)) },
                    error = { Icon(Icons.Outlined.Image, contentDescription = "Error", modifier = Modifier.size(40.dp), tint = Color.Gray) }
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.height(40.dp)
                )

                Text(
                    text = formatCurrencyVND(product.price),
                    color = Color(0xFFFF5722),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color(0xFFE1F5FE),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "Số Lượng: ${product.stock}",
                            color = Color(0xFF0277BD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = { onEditClick(product.id) },
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF03A9F4))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

fun formatCurrencyVND(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(amount)
}