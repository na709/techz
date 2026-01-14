package com.example.techz.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.techz.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CartManager {
    // State chứa danh sách sản phẩm trong giỏ (Tự động update UI Jetpack Compose)
    val cartItems = mutableStateListOf<CartItem>()

    // Hàm tính tổng tiền gốc (Chưa trừ giảm giá)
    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    // ------------------------------------------------------------------
    // 1. Load giỏ hàng từ Server
    // ------------------------------------------------------------------
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
                // Có thể log lỗi ở đây
            }
        })
    }

    // ------------------------------------------------------------------
    // 2. Cập nhật số lượng
    // ------------------------------------------------------------------
    fun updateQuantity(context: Context, productId: Int, change: Int) {
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index == -1) return

        val currentItem = cartItems[index]
        val newQuantity = currentItem.quantity + change
        if (newQuantity < 1) return

        // Cập nhật UI ngay lập tức (Optimistic Update)
        cartItems[index] = currentItem.copy(quantity = newQuantity)

        val userId = UserSession.currentUserId ?: return
        RetrofitClient.instance.updateQuantity(CartRequest(userId, productId, newQuantity)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful) {
                    // Lỗi server -> Rollback lại số lượng cũ
                    cartItems[index] = currentItem
                    Toast.makeText(context, "Lỗi cập nhật: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                // Lỗi mạng -> Rollback
                cartItems[index] = currentItem
            }
        })
    }

    // ------------------------------------------------------------------
    // 3. Xóa sản phẩm
    // ------------------------------------------------------------------
    fun removeProduct(context: Context, productId: Int) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Backup để rollback nếu lỗi
        val itemBackup = cartItems.find { it.product.id == productId }

        // Xóa trên UI ngay
        cartItems.removeIf { it.product.id == productId }

        // Gọi API xóa
        RetrofitClient.instance.removeFromCart(CartRequest(userId, productId, 0)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful && itemBackup != null) {
                    cartItems.add(itemBackup) // Rollback
                    Toast.makeText(context, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                if (itemBackup != null) cartItems.add(itemBackup) // Rollback
                Toast.makeText(context, "Lỗi mạng, hoàn tác xóa", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ------------------------------------------------------------------
    // 4. Đặt hàng (Đã sửa lỗi duplicate code và syntax)
    // ------------------------------------------------------------------
    fun placeOrder(
        context: Context,
        id_phuong_thuc: Int, // 1: COD, 2: MOMO, 3: VNPAY...
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

        // 1. Tính toán giá tiền
        val rawTotal = getTotalPrice()
        val finalPrice = (rawTotal - discount).coerceAtLeast(0.0)

        // 2. Map dữ liệu cart sang model OrderDetailRequest
        val listOrderDetails = cartItems.map { item ->
            OrderDetailRequest(
                productId = item.product.id,
                quantity = item.quantity,
                price = item.product.price
            )
        }

        // 3. Tạo Request đặt hàng
        val orderRequest = OrderRequest(
            userId = userId,
            address = address,
            phone = phone,
            totalPrice = finalPrice,
            cartItems = listOrderDetails,
            voucherId = voucherId,
            discountAmount = discount,
            id_phuong_thuc = id_phuong_thuc
        )

        // 4. Gọi API Create Order
        RetrofitClient.instance.createOrder(orderRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    val orderId = body.orderId // Lấy OrderID từ server trả về

                    // --- XỬ LÝ THANH TOÁN ---
                    if (id_phuong_thuc == 2 && orderId != null) {
                        // Nếu chọn Ví Momo -> Gọi hàm thanh toán Momo
                        initiateMomoPayment(context, orderId, finalPrice.toLong(), onSuccess)
                    } else {
                        // Nếu là Tiền mặt (COD) hoặc ATM thường -> Thành công luôn
                        handleOrderSuccess(userId, context, onSuccess)
                    }
                } else {
                    Toast.makeText(context, body?.message ?: "Đặt hàng thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Hàm phụ trợ: Xử lý khi đặt hàng thành công (Xóa giỏ hàng, thông báo)
    private fun handleOrderSuccess(userId: Int, context: Context, onSuccess: () -> Unit) {
        cartItems.clear() // Xóa UI
        clearServerCart(userId) // Xóa Database Server
        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
        onSuccess() // Callback điều hướng về Home
    }

    // Hàm phụ trợ: Gọi API Momo
    private fun initiateMomoPayment(context: Context, orderId: Int, amount: Long, onSuccess: () -> Unit) {
        val req = MomoPaymentRequest(orderId.toString(), amount, "Thanh toan don #$orderId")
        RetrofitClient.instance.createMomoPayment(req).enqueue(object : Callback<MomoResponse> {
            override fun onResponse(call: Call<MomoResponse>, response: Response<MomoResponse>) {
                if (response.body()?.success == true) {
                    val payUrl = response.body()?.payUrl
                    if (!payUrl.isNullOrEmpty()) {
                        // Mở app Momo hoặc Web
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
                        context.startActivity(intent)

                        // Sau khi mở Momo thì coi như đặt hàng xong (Dọn dẹp giỏ hàng)
                        // (Lưu ý: Logic chuẩn cần check IPN callback, nhưng tạm thời clear luôn cho UX)
                        val userId = UserSession.currentUserId
                        if (userId != null) handleOrderSuccess(userId, context, onSuccess)
                    }
                } else {
                    Toast.makeText(context, "Lỗi tạo link Momo: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<MomoResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi mạng Momo", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Hàm phụ trợ: Xóa sạch giỏ hàng trên Server sau khi đặt hàng
    private fun clearServerCart(userId: Int) {
        RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
            override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
        })
    }
}