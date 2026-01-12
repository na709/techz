package com.example.techz.ui.screens.cart

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.techz.model.CartItem
import com.example.techz.service.UserSession
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit,
    onMissingInfo: () -> Unit
) {
    val context = LocalContext.current
    val cartItems = CartManager.cartItems
    val totalPrice = CartManager.getTotalPrice()

    // Biến chọn phương thức thanh toán
    var selectedMethod by remember { mutableStateOf("Tiền mặt") }

    LaunchedEffect(Unit) {
        UserSession.initSession(context)
        CartManager.loadCart(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Giỏ Hàng", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF00A9FF))
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column {
                        PaymentMethodSelector(
                            currentMethod = selectedMethod,
                            onMethodChanged = { newMethod -> selectedMethod = newMethod }
                        )

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                        MinimalPaymentBottomBar(
                            totalPrice = totalPrice,
                            onCheckoutClick = {
                                UserSession.initSession(context)
                                if (!UserSession.isLoggedIn) {
                                    Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                                    onRequireLogin()
                                } else {
                                    val currentAddress = UserSession.currentUserAddress
                                    val currentPhone = UserSession.currentUserPhone

                                    if (currentAddress.isNullOrBlank() || currentPhone.isNullOrBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Vui lòng cập nhật Địa chỉ và SĐT để mua hàng!",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        onMissingInfo()
                                    } else {
                                        // 3. Đủ thông tin thì đặt hàng
                                        CartManager.placeOrder(context, selectedMethod) {
                                            Toast.makeText(
                                                context,
                                                "Đặt hàng thành công!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onCheckout()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Giỏ hàng trống", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(item, context)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, context: Context) {
    val baseUrl = "http://160.250.247.5/images/"
    val rawImageName = item.product.image ?: ""
    val fullImageUrl = if (rawImageName.startsWith("http")) rawImageName else baseUrl + rawImageName
    // -----------------

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = fullImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White),
                contentScale = ContentScale.Fit,
                error = painterResource(android.R.drawable.ic_menu_report_image)
            )

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
                Text(
                    "Giá : ${NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(item.product.price)}",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //
                    IconButton(onClick = { CartManager.updateQuantity(context, item.product.id, -1) }) {
                        Text("—", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { CartManager.updateQuantity(context, item.product.id, 1) }) {
                        Text("+", fontSize = 20.sp)
                    }
                }
            }
            IconButton(
                onClick = {
                    CartManager.removeProduct(context, item.product.id)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa",
                    tint = Color.Gray
                )
            }
        }
    }

}

@Composable
fun PaymentMethodSelector(
    currentMethod: String,
    onMethodChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val methods = listOf("Tiền mặt", "Chuyển khoản Ngân hàng", "Ví Momo", "ZaloPay")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp) // Căn chỉnh padding cho gọn
    ) {
        Text("Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = currentMethod)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                methods.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = {
                            onMethodChanged(method)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun MinimalPaymentBottomBar(
    totalPrice: Double,
    onCheckoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Tổng thanh toán:", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalPrice),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }

        Button(
            onClick = onCheckoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text("ĐẶT HÀNG", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}