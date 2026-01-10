package com.example.techz.ui.screens.product
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.ui.components.ProductItem
import com.example.techz.ui.components.TechZBottomBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// --- 1. Danh sách loại linh kiện (Hardcode theo API) ---
val CATEGORIES = listOf(
    "ThietBiMang", "RAM", "VGA", "Ghe", "Case",
    "Nguon","TanNhiet", "Micro", "Webcam", "TaiNghe","Chuot",
    "BanPhim", "SSD", "HDD", "Mainboard", "CPU", "Laptop"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ProductListScreen(navController: NavHostController,initialCategory: String? = null, onProductClick: (Int) -> Unit) { // Sửa String -> Int
    var originalList by remember { mutableStateOf<List<Product>>(emptyList()) }
    val context = LocalContext.current
    var currentName by remember { mutableStateOf<String?>(null) }
    var productList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // --- State cho bộ lọc ---
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(initialCategory) } // Biến lưu loại linh kiện đang chọn

    // Gọi API lấy danh sách sản phẩm
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("MY_APP_PREF", Context.MODE_PRIVATE)
        currentName = sharedPref.getString("USER_NAME", null)

        RetrofitClient.instance.getListProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    originalList = response.body() ?: emptyList()
                }
                isLoading = false
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("ProductList", "Error: ${t.message}")
                isLoading = false
            }
        })
    }

    // --- 2. Logic Lọc sản phẩm ---
    val filteredList = remember(originalList, searchQuery, sortOrder, minPrice, maxPrice, selectedCategory) {
        var result = originalList

        // Lọc theo tên
        if (searchQuery.isNotEmpty()) {
            result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        // Lọc theo Loại linh kiện (Sử dụng 'category' khớp với Model Product)
        if (selectedCategory != null) {
            result = result.filter { it.category == selectedCategory }
        }

        // Lọc theo khoảng giá
        val min = minPrice.toDoubleOrNull() ?: 0.0
        val max = maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
        result = result.filter { it.price >= min && it.price <= max }

        // Sắp xếp
        result = when (sortOrder) {
            SortOrder.PRICE_ASC -> result.sortedBy { it.price }
            SortOrder.PRICE_DESC -> result.sortedByDescending { it.price }
            SortOrder.NONE -> result
        }
        result
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00A9FF))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TechZ Store", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val focusManager = LocalFocusManager.current
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm kiếm sản phẩm...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .size(50.dp)
                    ) {
                        // Kiểm tra xem có bộ lọc nào đang bật không để đổi màu icon
                        val isFilterActive = selectedCategory != null || minPrice.isNotEmpty() || maxPrice.isNotEmpty() || sortOrder != SortOrder.NONE
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (isFilterActive) Color(0xFFFF5722) else Color(0xFF00A9FF)
                        )
                    }
                }
            }
        },
        bottomBar = { TechZBottomBar(navController, currentName) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filteredList.isEmpty()) {
                Text(
                    text = "Không tìm thấy sản phẩm nào",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { product ->
                        ProductItem(product, onProductClick)
                    }
                }
            }
        }

        // --- Bottom Sheet Bộ lọc ---
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = Color.White
            ) {
                FilterSheetContent(
                    currentSort = sortOrder,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    currentCategory = selectedCategory,
                    onApply = { sort, min, max, cat ->
                        sortOrder = sort
                        minPrice = min
                        maxPrice = max
                        selectedCategory = cat
                        showFilterSheet = false
                    },
                    onClear = {
                        sortOrder = SortOrder.NONE
                        minPrice = ""
                        maxPrice = ""
                        selectedCategory = null
                        showFilterSheet = false
                    }
                )
            }
        }
    }
}

enum class SortOrder { NONE, PRICE_ASC, PRICE_DESC }

@OptIn(ExperimentalLayoutApi::class) // Cần thiết cho FlowRow
@Composable
fun FilterSheetContent(
    currentSort: SortOrder,
    minPrice: String,
    maxPrice: String,
    currentCategory: String?,
    onApply: (SortOrder, String, String, String?) -> Unit,
    onClear: () -> Unit
) {
    // State cục bộ trong Sheet để người dùng thao tác trước khi ấn "Áp dụng"
    var localSort by remember { mutableStateOf(currentSort) }
    var localMin by remember { mutableStateOf(minPrice) }
    var localMax by remember { mutableStateOf(maxPrice) }
    var localCategory by remember { mutableStateOf(currentCategory) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // Cho phép cuộn nếu nội dung dài
    ) {
        Text("Bộ lọc & Sắp xếp", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // --- UI Chọn Loại linh kiện ---
        Text("Loại linh kiện", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CATEGORIES.forEach { category ->
                val isSelected = localCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // Toggle: Nếu đang chọn thì bỏ chọn, ngược lại thì chọn
                        localCategory = if (isSelected) null else category
                    },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00A9FF).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF00A9FF)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        // --- UI Sắp xếp ---
        Text("Sắp xếp theo giá", fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = localSort == SortOrder.PRICE_ASC,
                onClick = { localSort = SortOrder.PRICE_ASC },
                label = { Text("Thấp đến Cao") }
            )
            FilterChip(
                selected = localSort == SortOrder.PRICE_DESC,
                onClick = { localSort = SortOrder.PRICE_DESC },
                label = { Text("Cao đến Thấp") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- UI Khoảng giá ---
        Text("Khoảng giá (VNĐ)", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = localMin,
                onValueChange = { if (it.all { char -> char.isDigit() }) localMin = it },
                label = { Text("Tối thiểu") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Text(" - ", modifier = Modifier.padding(horizontal = 8.dp))
            OutlinedTextField(
                value = localMax,
                onValueChange = { if (it.all { char -> char.isDigit() }) localMax = it },
                label = { Text("Tối đa") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Nút bấm ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                Text("Xóa bộ lọc")
            }
            Button(
                onClick = { onApply(localSort, localMin, localMax, localCategory) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A9FF))
            ) {
                Text("Áp dụng")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}