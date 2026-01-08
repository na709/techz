package com.example.techz.ui.screens.product
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavHostController, onProductClick: (Int) -> Unit) { // Sửa String -> Int
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
                Log.e("ProductList", "Lỗi: ${t.message}")
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tất cả linh kiện") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0066FF),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { TechZBottomBar(navController, currentName) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (productList.isEmpty()) {
                Text(
                    text = "Không có sản phẩm nào",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productList) { product ->
                        ProductItem(product, onProductClick)
                    }
                }
            }
        }
    }
}