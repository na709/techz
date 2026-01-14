package com.example.techz.ui.screens.product

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import com.example.techz.utils.getCategoryName
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Enum này có thể để ở file chung hoặc giữ ở đây
enum class SortOrder { NONE, PRICE_ASC, PRICE_DESC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    navController: NavHostController,
    categoryType: String? = null,
    onProductClick: (Int) -> Unit,
    onGoToCart: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var rawList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentName by remember { mutableStateOf<String?>(null) }
    var currentCategory by remember { mutableStateOf(categoryType ?: "All") }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // --- STATE CLIENT FILTER --- (Cần đưa lên server)
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }

    fun loadDataFromApi(isSearching: Boolean = false, keyword: String = "") {
        isLoading = true
        val api = RetrofitClient.instance
        val call: Call<List<Product>>

        if (isSearching && keyword.isNotBlank()) {
            call = api.searchByName(keyword)
        } else {
            call = if (currentCategory != "All") {
                api.filterByCategory(currentCategory)
            } else {
                api.getListProducts()
            }
        }
        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) rawList = response.body() ?: emptyList()
                else rawList = emptyList()
                isLoading = false
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                isLoading = false
            }
        })
    }

    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        currentName = sharedPref.getString("USER_NAME", null)
        loadDataFromApi(isSearching = false)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500)
            loadDataFromApi(isSearching = true, keyword = searchQuery)
        } else {
            delay(300)
            if (!isLoading) loadDataFromApi(isSearching = false)
        }
    }

    val displayList = remember(rawList, sortOrder, minPrice, maxPrice) {
        var result = rawList
        val min = minPrice.toDoubleOrNull() ?: 0.0
        val max = maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
        result = result.filter { it.price >= min && it.price <= max }
        result = when (sortOrder) {
            SortOrder.PRICE_ASC -> result.sortedBy { it.price }
            SortOrder.PRICE_DESC -> result.sortedByDescending { it.price }
            SortOrder.NONE -> result
        }
        result
    }

    val screenTitle = remember(currentCategory) {
        if (currentCategory == "All") "Sản phẩm" else getCategoryName(currentCategory)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onGoToCart) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00A9FF))
                )
            },
            bottomBar = { TechZBottomBar(navController, currentName) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tên sản phẩm...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).size(50.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = Color(0xFF00A9FF))
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00A9FF))
                    } else if (displayList.isEmpty()) {
                        Text("Không tìm thấy sản phẩm", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(displayList) { product -> ProductItem(product, onProductClick) }
                        }
                    }
                }
            }
        }
        FilterDrawer(
            isVisible = showFilterSheet,
            currentCategory = currentCategory,
            currentSort = sortOrder,
            currentMin = minPrice,
            currentMax = maxPrice,
            onDismissRequest = { showFilterSheet = false },
            onApply = { newCat, newSort, newMin, newMax ->
                currentCategory = newCat
                sortOrder = newSort
                minPrice = newMin
                maxPrice = newMax
                loadDataFromApi(isSearching = false)
                searchQuery = ""
                showFilterSheet = false
            }
        )
    }
}