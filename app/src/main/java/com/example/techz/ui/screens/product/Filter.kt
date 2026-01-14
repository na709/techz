package com.example.techz.ui.screens.product

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techz.service.RetrofitClient
import com.example.techz.utils.getCategoryName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterDrawer(
    isVisible: Boolean,
    currentCategory: String,
    currentSort: SortOrder,
    currentMin: String,
    currentMax: String,
    onDismissRequest: () -> Unit,
    onApply: (String, SortOrder, String, String) -> Unit
) {
    var apiCategories by remember { mutableStateOf<List<String>>(emptyList()) }

    var tempCategory by remember(isVisible) { mutableStateOf(currentCategory) }
    var tempSort by remember(isVisible) { mutableStateOf(currentSort) }
    var tempMin by remember(isVisible) { mutableStateOf(currentMin) }
    var tempMax by remember(isVisible) { mutableStateOf(currentMax) }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    apiCategories = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("FilterDrawer", "Lỗi lấy danh mục: ${t.message}")
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onDismissRequest() }
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
                    .background(Color.White)
                    .clickable(enabled = false) {}
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bộ lọc", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                tempCategory = "All"
                                tempSort = SortOrder.NONE
                                tempMin = ""
                                tempMax = ""
                            }
                        ) {
                            Text("Xóa", color = Color.Gray)
                        }

                        Button(
                            onClick = { onApply(tempCategory, tempSort, tempMin, tempMax) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A9FF)),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Áp dụng", fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Loại linh kiện:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tempCategory == "All",
                        onClick = { tempCategory = "All" },
                        label = { Text("Tất cả") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00A9FF).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF00A9FF)
                        )
                    )

                    apiCategories.forEach { cat ->
                        FilterChip(
                            selected = tempCategory == cat,
                            onClick = { tempCategory = cat },
                            label = { Text(getCategoryName(cat)) },
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
                Text("Sắp xếp giá:", fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tempSort == SortOrder.PRICE_ASC,
                        onClick = { tempSort = if (tempSort == SortOrder.PRICE_ASC) SortOrder.NONE else SortOrder.PRICE_ASC },
                        label = { Text("Thấp -> Cao") }
                    )
                    FilterChip(
                        selected = tempSort == SortOrder.PRICE_DESC,
                        onClick = { tempSort = if (tempSort == SortOrder.PRICE_DESC) SortOrder.NONE else SortOrder.PRICE_DESC },
                        label = { Text("Cao -> Thấp") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Khoảng giá (VNĐ):", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tempMin,
                        onValueChange = { if (it.all { c -> c.isDigit() }) tempMin = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Text(" - ", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = tempMax,
                        onValueChange = { if (it.all { c -> c.isDigit() }) tempMax = it },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}