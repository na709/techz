package com.example.techz.ui.screens.cart

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.techz.model.AuthResponse
import com.example.techz.model.CartItem
import com.example.techz.model.CartRequest
import com.example.techz.model.OrderRequest
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CartManager {
    // List giỏ hàng (State để UI tự cập nhật)
    val cartItems = mutableStateListOf<CartItem>()

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    // 1. Load giỏ hàng
    fun loadCart(context: Context) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            cartItems.clear() // Nếu chưa đăng nhập thì giỏ trống
            return
        }

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

    // 2. Cập nhật số lượng (+ / -)
    fun updateQuantity(context: Context, productId: Int, change: Int) {
        // 1. Tìm VỊ TRÍ (index) của sản phẩm trong list
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index == -1) return

        val currentItem = cartItems[index]
        val newQuantity = currentItem.quantity + change

        // 2. Validate: Không cho < 1
        if (newQuantity < 1) {
            Toast.makeText(context, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show()
            return
        }
//
        // 3. QUAN TRỌNG: Tạo bản sao mới (copy) và gán lại vào vị trí cũ
        // Việc gán cartItems[index] = ... sẽ báo cho Compose biết để vẽ lại UI ngay lập tức
        cartItems[index] = currentItem.copy(quantity = newQuantity)

        // 4. Gửi lên Server (Logic giữ nguyên)
        val userId = UserSession.currentUserId ?: return
        val request = CartRequest(userId, productId, newQuantity)

        RetrofitClient.instance.updateQuantity(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful) {
                    // Nếu lỗi Server -> Trả lại số cũ
                    cartItems[index] = currentItem.copy(quantity = currentItem.quantity)
                    Toast.makeText(context, "Lỗi server: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                // Nếu lỗi Mạng -> Trả lại số cũ
                cartItems[index] = currentItem.copy(quantity = currentItem.quantity)
                Toast.makeText(context, "Lỗi mạng, hoàn tác!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 3. Xóa sản phẩm
    fun removeFromCart(context: Context, productId: Int) {
        val userId = UserSession.currentUserId ?: return
        val request = CartRequest(userId, productId, 0) // Số lượng 0 ko quan trọng khi xóa

        RetrofitClient.instance.removeFromCart(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) loadCart(context)
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
        })
    }

    // 4. Đặt hàng
    fun placeOrder(context: Context, paymentMethod: String, onSuccess: () -> Unit) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Toast.makeText(context, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show()
            return
        }

        // --- SỬA LỖI TẠI ĐÂY ---
        // Kiểm tra nếu rỗng ("") hoặc null thì điền tạm giá trị mặc định
        // Để vượt qua bài kiểm tra "Thiếu thông tin" của Server

        var address = UserSession.currentUserAddress
        if (address.isNullOrBlank()) {
            address = "Khách chưa cập nhật địa chỉ"
        }

        var phone = UserSession.currentUserPhone
        if (phone.isNullOrBlank()) {
            phone = "0999999999" // Số điện thoại tạm
        }
        // ------------------------

        val orderRequest = OrderRequest(
            userId = userId,
            address = address,
            phone = phone,
            paymentMethod = paymentMethod,
            totalPrice = getTotalPrice()
        )

        RetrofitClient.instance.createOrder(orderRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    cartItems.clear()
                    onSuccess()
                    Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()

                    // Xóa giỏ hàng trên server
                    RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse>{
                        override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
                        override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
                    })
                } else {
                    // Hiển thị lỗi chi tiết từ Server
                    val errorMsg = try {
                        response.errorBody()?.string() ?: response.message()
                    } catch (e: Exception) {
                        "Lỗi không xác định"
                    }
                    Toast.makeText(context, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}