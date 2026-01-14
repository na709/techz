package com.example.techz.ui.screens.admin


import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.model.AuthResponse
import com.example.techz.model.VoucherRequest
import com.example.techz.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVoucherScreen(
    navController: NavHostController,
    onBack :() -> Unit
    ) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var percent by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var minOrder by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val formattedDate = "$year-${month + 1}-$day"
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun submitVoucher() {
        if (code.isEmpty() || name.isEmpty() || percent.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val request = VoucherRequest(
            code = code,
            name = name,
            discountPercent = percent.toIntOrNull() ?: 0,
            maxDiscountAmount = maxDiscount.toDoubleOrNull() ?: 0.0,
            minOrderValue = minOrder.toDoubleOrNull() ?: 0.0,
            quantity = quantity.toIntOrNull() ?: 100,
            startDate = startDate,
            endDate = endDate
        )

        RetrofitClient.instance.addVoucher(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                isLoading = false
                if (response.isSuccessful) {
                    Toast.makeText(context, "Tạo Voucher thành công!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Lỗi: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Mã Giảm Giá", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00A9FF))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Mã Voucher (VD: TET2025)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên chương trình") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = percent,
                    onValueChange = { percent = it },
                    label = { Text("% Giảm") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Số lượng") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = maxDiscount,
                onValueChange = { maxDiscount = it },
                label = { Text("Giảm tối đa (VNĐ)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = minOrder,
                onValueChange = { minOrder = it },
                label = { Text("Đơn hàng tối thiểu (VNĐ)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    label = { Text("Ngày bắt đầu") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker { date -> startDate = date } },
                    enabled = false,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray
                    ),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null) }
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    label = { Text("Ngày kết thúc") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker { date -> endDate = date } },
                    enabled = false,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray
                    ),
                    trailingIcon = { Icon(Icons.Default.DateRange, null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { submitVoucher() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A9FF)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Tạo Voucher", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}