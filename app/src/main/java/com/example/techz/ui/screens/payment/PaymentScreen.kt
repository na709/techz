package com.example.techz.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onConfirm: () -> Unit, onBack: () -> Unit) {
    var method by remember { mutableStateOf("COD") }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Thanh toán") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Chọn phương thức:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            listOf("Ship COD", "Ví MoMo", "Ngân hàng").forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (m == method), onClick = { method = m })
                    Text(m)
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))) { Text("XÁC NHẬN ($method)") }
        }
    }
}