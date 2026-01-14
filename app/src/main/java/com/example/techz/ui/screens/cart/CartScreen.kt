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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.techz.model.CartItem
import com.example.techz.model.PaymentMethod
import com.example.techz.model.Voucher
import com.example.techz.service.CartManager
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    // Dữ liệu giỏ hàng (Reactive State từ CartManager)
    val cartItems = CartManager.cartItems
    val rawTotalPrice = CartManager.getTotalPrice() // Giá gốc (chưa trừ voucher)

    // --- STATE QUẢN LÝ VOUCHER ---
    var availableVouchers by remember { mutableStateOf<List<Voucher>>(emptyList()) }
    var selectedVoucher by remember { mutableStateOf<Voucher?>(null) }
    var discountAmount by remember { mutableStateOf(0.0) }
    var showVoucherDialog by remember { mutableStateOf(false) }

    // --- STATE THANH TOÁN ---
    var paymentMethods by remember { mutableStateOf<List<PaymentMethod>>(emptyList()) }
    var selectedMethodObj by remember { mutableStateOf<PaymentMethod?>(null) }

    val finalPrice = (rawTotalPrice - discountAmount).coerceAtLeast(0.0)

    // --- 1. KHỞI TẠO DỮ LIỆU ---
    LaunchedEffect(Unit) {
        UserSession.initSession(context)
        CartManager.loadCart(context)

        // Nếu đã đăng nhập -> Gọi API lấy Voucher khả dụng
        if (UserSession.isLoggedIn) {
            RetrofitClient.instance.getPaymentMethods().enqueue(object : Callback<List<PaymentMethod>> {
                override fun onResponse(call: Call<List<PaymentMethod>>, response: Response<List<PaymentMethod>>) {
                    if (response.isSuccessful) {
                        val methods = response.body() ?: emptyList()
                        paymentMethods = methods
                        // Mặc định chọn cái đầu tiên (thường là COD)
                        if (methods.isNotEmpty()) selectedMethodObj = methods[0]
                    }
                }
                override fun onFailure(call: Call<List<PaymentMethod>>, t: Throwable) {}
            })
            RetrofitClient.instance.getAvailableVouchers().enqueue(object : Callback<List<Voucher>> {
                override fun onResponse(call: Call<List<Voucher>>, response: Response<List<Voucher>>) {
                    if (response.isSuccessful) {
                        availableVouchers = response.body() ?: emptyList()
                    }
                }
                override fun onFailure(call: Call<List<Voucher>>, t: Throwable) {
                }
            })
        }
    }

    // --- 2. LOGIC TỰ ĐỘNG CẬP NHẬT KHI GIÁ THAY ĐỔI ---
    // Nếu người dùng xóa bớt sản phẩm khiến tổng tiền < đơn tối thiểu -> Hủy voucher
    LaunchedEffect(rawTotalPrice) {
        selectedVoucher?.let { voucher ->
            if (rawTotalPrice < voucher.minOrder) {
                selectedVoucher = null
                discountAmount = 0.0
                Toast.makeText(context, "Đơn hàng không còn đủ điều kiện dùng Voucher", Toast.LENGTH_SHORT).show()
            } else {
                // Tính lại tiền giảm (vì % giảm dựa trên tổng tiền mới)
                val calculated = rawTotalPrice * voucher.percent / 100
                discountAmount = if (calculated > voucher.maxDiscount) voucher.maxDiscount else calculated
            }
        }
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
                        // A. MỤC CHỌN VOUCHER
                        VoucherSelector(
                            selectedVoucher = selectedVoucher,
                            discountValue = discountAmount,
                            onClick = {
                                if (UserSession.isLoggedIn) {
                                    showVoucherDialog = true
                                } else {
                                    Toast.makeText(context, "Vui lòng đăng nhập để dùng Voucher", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        HorizontalDivider(thickness = 4.dp, color = Color(0xFFEEEEEE))

                        // B. PHƯƠNG THỨC THANH TOÁN
                        PaymentMethodSelector(
                            currentMethod = selectedMethodObj?.ten_phuong_thuc ?: "Đang tải...",
                            methods = paymentMethods,
                            onMethodChanged = { newMethod -> selectedMethodObj = newMethod }
                        )

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                        // C. THANH TOÁN & ĐẶT HÀNG
                        MinimalPaymentBottomBar(
                            totalPrice = finalPrice,
                            originalPrice = rawTotalPrice,
                            onCheckoutClick = {
                                CartManager.placeOrder(
                                    context = context,
                                    id_phuong_thuc = selectedMethodObj?.id_phuong_thuc ?: 1,
                                    voucherId = selectedVoucher?.id,
                                    discount = discountAmount
                                ) {
                                    Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_LONG).show()
                                    onCheckout()
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

    // --- DIALOG CHỌN VOUCHER ---
    if (showVoucherDialog) {
        VoucherSelectionDialog(
            vouchers = availableVouchers,
            onDismiss = { showVoucherDialog = false },
            onSelect = { voucher ->
                // Kiểm tra điều kiện đơn tối thiểu
                if (rawTotalPrice < voucher.minOrder) {
                    Toast.makeText(context, "Đơn hàng chưa đủ ${formatCurrency(voucher.minOrder)} để dùng mã này!", Toast.LENGTH_SHORT).show()
                } else {
                    // Tính tiền giảm
                    val calculated = rawTotalPrice * voucher.percent / 100
                    val finalDiscount = if (calculated > voucher.maxDiscount) voucher.maxDiscount else calculated

                    selectedVoucher = voucher
                    discountAmount = finalDiscount
                    showVoucherDialog = false
                }
            },
            onRemove = {
                selectedVoucher = null
                discountAmount = 0.0
                showVoucherDialog = false
            }
        )
    }
}

// =========================================================================
// CÁC COMPONENT CON (UI)
// =========================================================================

@Composable
fun CartItemRow(item: CartItem, context: Context) {
    // Đảm bảo URL ảnh đúng với Server của bạn
    val baseUrl = "http://103.228.36.78:3000/images/"
    val rawImageName = item.product.image ?: ""
    val fullImageUrl = if (rawImageName.startsWith("http")) rawImageName else baseUrl + rawImageName

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
                modifier = Modifier.size(90.dp).background(Color.White),
                contentScale = ContentScale.Fit,
                error = painterResource(android.R.drawable.ic_menu_report_image)
            )

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
                Text(
                    "Giá: ${formatCurrency(item.product.price)}",
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { CartManager.updateQuantity(context, item.product.id, -1) }) {
                        Text("—", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { CartManager.updateQuantity(context, item.product.id, 1) }) {
                        Text("+", fontSize = 20.sp)
                    }
                }
            }
            IconButton(onClick = { CartManager.removeProduct(context, item.product.id) }) {
                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun VoucherSelector(
    selectedVoucher: Voucher?,
    discountValue: Double,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_agenda),
                contentDescription = null,
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            if (selectedVoucher == null) {
                Text("TechZ Voucher", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            } else {
                Column {
                    Text("Đã chọn: ${selectedVoucher.code}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A9FF))
                    Text("-${formatCurrency(discountValue)}", fontSize = 13.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (selectedVoucher == null) "Chọn hoặc nhập mã" else "Thay đổi",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun PaymentMethodSelector(
    currentMethod: String,
    methods: List<PaymentMethod>,
    onMethodChanged: (PaymentMethod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                        text = { Text(method.ten_phuong_thuc) },
                        onClick = { onMethodChanged(method); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalPaymentBottomBar(
    totalPrice: Double,
    originalPrice: Double,
    onCheckoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Tổng thanh toán:", fontSize = 14.sp, color = Color.Gray)

            // Nếu có giảm giá -> Hiện giá gốc gạch ngang
            if (originalPrice > totalPrice) {
                Text(
                    text = formatCurrency(originalPrice),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
                )
            }

            Text(
                text = formatCurrency(totalPrice),
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

@Composable
fun VoucherSelectionDialog(
    vouchers: List<Voucher>,
    onDismiss: () -> Unit,
    onSelect: (Voucher) -> Unit,
    onRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn TechZ Voucher", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Không sử dụng khuyến mãi", color = Color.Red)
                }

                if (vouchers.isEmpty()) {
                    Text("Không có voucher nào khả dụng.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(vouchers) { voucher ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { onSelect(voucher) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(voucher.code, fontWeight = FontWeight.Bold, color = Color(0xFF00A9FF), fontSize = 16.sp)
                                        Text(voucher.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Đơn tối thiểu: ${formatCurrency(voucher.minOrder)}", fontSize = 12.sp, color = Color.Gray)
                                        Text("Giảm tối đa: ${formatCurrency(voucher.maxDiscount)}", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    // Badge %
                                    Surface(
                                        color = Color(0xFFFFE0B2),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "-${voucher.percent}%",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF5722),
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
}