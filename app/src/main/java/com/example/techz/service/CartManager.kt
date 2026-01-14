package com.example.techz.service

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.techz.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CartManager {
    val cartItems = mutableStateListOf<CartItem>()

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    fun loadCart(context: Context) {
        val userId = UserSession.currentUserId ?: return
        RetrofitClient.instance.getCart(userId).enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    cartItems.clear()
                    response.body()?.let { cartItems.addAll(it) }
                }
            }
            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {}
        })
    }

    fun updateQuantity(context: Context, productId: Int, change: Int) {
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index == -1) return
        val currentItem = cartItems[index]
        val newQuantity = currentItem.quantity + change
        if (newQuantity < 1) return

        cartItems[index] = currentItem.copy(quantity = newQuantity)
        val userId = UserSession.currentUserId ?: return
        RetrofitClient.instance.updateQuantity(CartRequest(userId, productId, newQuantity)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful) cartItems[index] = currentItem
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                cartItems[index] = currentItem
            }
        })
    }

    fun removeProduct(context: Context, productId: Int) {
        val userId = UserSession.currentUserId ?: return
        val itemBackup = cartItems.find { it.product.id == productId }
        cartItems.removeIf { it.product.id == productId }
        RetrofitClient.instance.removeFromCart(CartRequest(userId, productId, 0)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful && itemBackup != null) cartItems.add(itemBackup)
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                if (itemBackup != null) cartItems.add(itemBackup)
            }
        })
    }

    // --- HÀM PLACE ORDER ĐÃ FIX ---
    fun placeOrder(
        context: Context,
        id_phuong_thuc: Int, // Nhận ID (1, 2, 3)
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
            Toast.makeText(context, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show()
            return
        }

        val address = UserSession.currentUserAddress
        val phone = UserSession.currentUserPhone

        if (address.isNullOrBlank() || phone.isNullOrBlank()) {
            Toast.makeText(context, "Vui lòng cập nhật Địa chỉ và SĐT!", Toast.LENGTH_LONG).show()
            return
        }

        val rawTotal = getTotalPrice()
        val finalPrice = (rawTotal - discount).coerceAtLeast(0.0)

        // Map data chi tiết
        val listItems = cartItems.map {
            OrderDetailRequest(it.product.id, it.quantity, it.product.price)
        }

        // Tạo Request chuẩn
        val request = OrderRequest(
            userId = userId,
            address = address,
            phone = phone,
            totalPrice = finalPrice,
            cartItems = listItems,
            voucherId = voucherId,
            discountAmount = discount,
            id_phuong_thuc = id_phuong_thuc // <--- QUAN TRỌNG: Gửi ID này đi
        )

        RetrofitClient.instance.createOrder(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val orderId = response.body()?.orderId

                    // ID = 2 là Momo
                    if (id_phuong_thuc == 2 && orderId != null) {
                        initiateMomoPayment(context, orderId, finalPrice.toLong(), onSuccess)
                    } else {
                        // COD hoặc ATM
                        cartItems.clear()
                        clearServerCart(userId)
                        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Lỗi Server", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initiateMomoPayment(context: Context, orderId: Int, amount: Long, onSuccess: () -> Unit) {
        val req = MomoPaymentRequest(orderId.toString(), amount, "Thanh toan don #$orderId")
        RetrofitClient.instance.createMomoPayment(req).enqueue(object : Callback<MomoResponse> {
            override fun onResponse(call: Call<MomoResponse>, response: Response<MomoResponse>) {
                if (response.body()?.success == true) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(response.body()?.payUrl))
                    context.startActivity(intent)
                    cartItems.clear()
                    UserSession.currentUserId?.let { clearServerCart(it) }
                    onSuccess()
                } else {
                    Toast.makeText(context, "Lỗi Momo: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MomoResponse>, t: Throwable) {}
        })
    }

    private fun clearServerCart(userId: Int) {
        RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
            override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
        })
    }
}