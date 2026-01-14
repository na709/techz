package com.example.techz.service

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.techz.model.AuthResponse
import com.example.techz.model.CartItem
import com.example.techz.model.CartRequest
import com.example.techz.model.OrderDetailRequest
import com.example.techz.model.OrderRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CartManager {
    val cartItems = mutableStateListOf<CartItem>()

    // Hàm tính tổng tiền gốc (Chưa trừ giảm giá)
    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    // 1. Load giỏ hàng từ Server
    fun loadCart(context: Context) {
        val userId = UserSession.currentUserId ?: return

        RetrofitClient.instance.getCart(userId).enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    cartItems.clear()
                    response.body()?.let { cartItems.addAll(it) }
                }
            }
            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                // Log lỗi nếu cần
            }
        })
    }

    // 2. Cập nhật số lượng
    fun updateQuantity(context: Context, productId: Int, change: Int) {
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index == -1) return

        val currentItem = cartItems[index]
        val newQuantity = currentItem.quantity + change

        if (newQuantity < 1) {
            Toast.makeText(context, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show()
            return
        }

        // Cập nhật UI ngay lập tức (Optimistic Update)
        cartItems[index] = currentItem.copy(quantity = newQuantity)

        val userId = UserSession.currentUserId ?: return
        val request = CartRequest(userId, productId, newQuantity)

        RetrofitClient.instance.updateQuantity(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful) {
                    // Lỗi server -> Trả lại số cũ
                    cartItems[index] = currentItem
                    Toast.makeText(context, "Lỗi cập nhật: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                cartItems[index] = currentItem
                Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 3. Xóa sản phẩm
    fun removeProduct(context: Context, productId: Int) {
        val userId = UserSession.currentUserId

        if (userId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Backup để rollback nếu lỗi
        val itemBackup = cartItems.find { it.product.id == productId }

        // Xóa UI ngay
        cartItems.removeIf { it.product.id == productId }

        val request = CartRequest(userId, productId, 0)

        RetrofitClient.instance.removeFromCart(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
                } else {
                    if (itemBackup != null) cartItems.add(itemBackup) // Rollback
                    Toast.makeText(context, "Lỗi xóa: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                if (itemBackup != null) cartItems.add(itemBackup)
                Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 4. Đặt hàng (Đã sửa lỗi thiếu tham số voucherId)
    fun placeOrder(
        context: Context,
        paymentMethod: String,
        voucherId: Int?,
        discount: Double,
        onSuccess: () -> Unit
    ) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Toast.makeText(context, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show()
            return
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show()
            return
        }

        val address = UserSession.currentUserAddress
        val phone = UserSession.currentUserPhone

        if (address.isNullOrBlank() || phone.isNullOrBlank()) {
            Toast.makeText(context, "Vui lòng cập nhật Địa chỉ và SĐT!", Toast.LENGTH_LONG).show()
            return
        }

        // 1. Tính giá cuối cùng
        val rawTotal = getTotalPrice()
        val finalPrice = (rawTotal - discount).coerceAtLeast(0.0)

        // 2. QUAN TRỌNG: Chuyển đổi từ CartItem sang OrderDetailRequest
        val listSanPhamGuiLenServer = cartItems.map { item ->
            OrderDetailRequest(
                productId = item.product.id,
                quantity = item.quantity,
                price = item.product.price
            )
        }

        // 3. Tạo Request khớp với Model OrderRequest mới của bạn
        val orderRequest = OrderRequest(
            userId = userId,
            address = address,
            phone = phone,
            paymentMethod = paymentMethod,
            totalPrice = finalPrice,

            cartItems = listSanPhamGuiLenServer, // <--- Truyền list đã convert vào đây

            voucherId = voucherId,
            discountAmount = discount
        )

        // 4. Gọi API
        RetrofitClient.instance.createOrder(orderRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val orderIdFromServer = response.body()?.orderId

                    if (paymentMethod == "Ví Momo" && orderIdFromServer != null) {
                        // Nếu chọn Momo -> Gọi hàm xử lý Momo riêng
                        initiateMomoPayment(context, orderIdFromServer, finalPrice.toLong(), onSuccess)
                    } else {
                        // Nếu là Tiền mặt (COD) -> Xử lý như cũ
                        cartItems.clear()
                        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                        clearServerCart(userId)
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Lỗi đặt hàng"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initiateMomoPayment(context: Context, orderId: Int, amount: Long, onSuccess: () -> Unit) {
        val request = com.example.techz.model.MomoPaymentRequest(
            orderId = orderId.toString(),
            amount = amount,
            orderInfo = "Thanh toan don hang #$orderId TechZ"
        )

        RetrofitClient.instance.createMomoPayment(request).enqueue(object : Callback<com.example.techz.model.MomoResponse> {
            override fun onResponse(call: Call<com.example.techz.model.MomoResponse>, response: Response<com.example.techz.model.MomoResponse>) {
                if (response.body()?.success == true) {
                    val payUrl = response.body()?.payUrl

                    if (payUrl != null) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(payUrl))
                        context.startActivity(intent)

                        cartItems.clear()
                        onSuccess()

                        UserSession.currentUserId?.let { clearServerCart(it) }
                    }
                } else {
                    Toast.makeText(context, "Lỗi tạo cổng thanh toán: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.techz.model.MomoResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi kết nối Momo: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Hàm phụ xóa giỏ hàng server
    private fun clearServerCart(userId: Int) {
        RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
            override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
        })
    }
}