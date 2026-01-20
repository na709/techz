package com.example.techz.ui.screens.product
//
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // Import NavController
import coil.compose.AsyncImage
import com.example.techz.model.AuthResponse
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.navigation.Screen // Import Screen để lấy route Login
import com.example.techz.service.CartManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import com.example.techz.model.CartRequest
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    navController: NavController,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val context = LocalContext.current
    var relatedProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    val scrollState = rememberScrollState()
    LaunchedEffect(product.category) {
        RetrofitClient.instance.getProductDetail(product.id).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val currentProduct = response.body()
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
            }
            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.e("ProductDetail", "Error: ${t.message}")
            }
        })
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                val isOutOfStock = product.stock <= 0
                val quantityInCart = CartManager.cartItems.find { it.product.id == product.id }?.quantity ?: 0
                Button(
                    onClick = {
                        val userId = UserSession.currentUserId
                        if (userId == null) {
                            Toast.makeText(context, "Vui lòng đăng nhập để mua hàng!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Login.route)
                        } else {
                            val request = CartRequest(
                                userId = userId,
                                productId = product.id,
                                quantity = 1
                            )

                            RetrofitClient.instance.addToCart(request).enqueue(object : Callback<AuthResponse> {
                                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show()
                                        CartManager.loadCart(context)
                                    } else {
                                        Toast.makeText(context, "Thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                    Toast.makeText(context, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = (product.stock >0)&& (quantityInCart<product.stock),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOutOfStock) Color.Gray else Color(0xFF00A9FF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Thêm vào giỏ hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(Color(0xFFF5F5F5))
        ) {
            // Header ảnh
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White)) {
                // Xử lý link ảnh
                val imageUrl = if (product.image?.startsWith("http") == true) product.image else "http://160.250.247.5/images/${product.image}"

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).background(Color.Black.copy(alpha = 0.1f), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }

            // Thông tin chi tiết
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(product.price),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Số lượng còn lại: ${product.stock}",
                    fontSize = 16.sp,
                    color = if (product.stock > 0) Color.Blue else Color.Red,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Mô tả sản phẩm:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description ?: "Không có mô tả",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
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
                                    ProductItem(
                                        product = item,
                                        onClick = { onProductClick(item) },
                                        onAddToCart = { selectedProduct ->

                                            val userId = UserSession.currentUserId

                                            if (userId == null) {
                                                Toast.makeText(context, "Vui lòng đăng nhập để mua hàng!", Toast.LENGTH_SHORT).show()

                                                navController.navigate("login")
                                            } else {
                                                val request = CartRequest(
                                                    userId = userId,
                                                    productId = selectedProduct.id,
                                                    quantity = 1
                                                )

                                                RetrofitClient.instance.addToCart(request).enqueue(object : Callback<AuthResponse> {
                                                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                                        if (response.isSuccessful) {
                                                            Toast.makeText(context, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Thất bại: ${response.message()}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }

                                                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                                        Toast.makeText(context, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                            }
                                        }
                                    )
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