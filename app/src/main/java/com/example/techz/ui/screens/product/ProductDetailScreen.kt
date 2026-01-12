package com.example.techz.ui.screens.product

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
import com.example.techz.ui.screens.cart.CartManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import com.example.techz.model.CartRequest
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    navController: NavController, // <-- Đã thêm tham số này
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val context = LocalContext.current
    var relatedProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    val scrollState = rememberScrollState()

    // Load sản phẩm liên quan
    LaunchedEffect(product.category) {
        // Lưu ý: server trả về category là String hay Int?
        // Trong file Product.kt bạn để category: String?, nhưng API thường dùng ID.
        // Tôi giữ nguyên logic cũ của bạn, chỉ thêm check null
        RetrofitClient.instance.getListProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    // Lọc sản phẩm cùng loại (logic tạm thời)
                    relatedProducts = response.body()?.filter { it.id != product.id }?.take(4) ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {}
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
                Button(
                    onClick = {
                        // 1. Kiểm tra đăng nhập
                        val userId = UserSession.currentUserId
                        if (userId == null) {
                            Toast.makeText(context, "Vui lòng đăng nhập để mua hàng!", Toast.LENGTH_SHORT).show()
                            // Có thể navigate về trang Login tại đây nếu muốn
                            navController.navigate(Screen.Login.route)
                        } else {
                            // 2. Tạo Request chuẩn
                            val request = CartRequest(
                                userId = userId,
                                productId = product.id,
                                quantity = 1
                            )

                            // 3. Gọi API
                            RetrofitClient.instance.addToCart(request).enqueue(object : Callback<AuthResponse> {
                                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show()
                                        // Load lại giỏ hàng ngầm để cập nhật số lượng badge (nếu có)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A9FF)),
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

            // Sản phẩm liên quan... (Giữ nguyên logic hiển thị của bạn)
            if (relatedProducts.isNotEmpty()) {
                // ... (Code hiển thị related products như cũ)
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("Sản phẩm gợi ý", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    relatedProducts.forEach { item ->
                        Text(item.name, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}