package com.example.techz.ui.screens.cart

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.techz.model.AuthResponse
import com.example.techz.model.CartItem
import com.example.techz.model.CartRequest
import com.example.techz.model.OrderDetailRequest // Import model chi tiết
import com.example.techz.model.OrderRequest
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CartManager {
    val cartItems = mutableStateListOf<CartItem>()

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    // 1. Load giỏ hàng
    fun loadCart(context: Context) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            cartItems.clear()
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
            }
        })
    }

    fun updateQuantity(context: Context, productId: Int, change: Int) {
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index == -1) return

        val currentItem = cartItems[index]
        val newQuantity = currentItem.quantity + change

        if (newQuantity < 1) {
            Toast.makeText(context, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show()
            return
        }

        cartItems[index] = currentItem.copy(quantity = newQuantity)

        val userId = UserSession.currentUserId ?: return
        val request = CartRequest(userId, productId, newQuantity)

        RetrofitClient.instance.updateQuantity(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (!response.isSuccessful) {
                    // Revert nếu lỗi
                    cartItems[index] = currentItem.copy(quantity = currentItem.quantity)
                    Toast.makeText(context, "Lỗi server: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                cartItems[index] = currentItem.copy(quantity = currentItem.quantity)
                Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun removeFromCart(context: Context, productId: Int) {
        val userId = UserSession.currentUserId ?: return
        val request = CartRequest(userId, productId, 0)

        RetrofitClient.instance.removeFromCart(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) loadCart(context)
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
        })
    }

    fun removeProduct(context: Context, productId: Int) {
        val userId = UserSession.currentUserId ?: 0

        if (userId <= 0) {
            Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo request gửi lên server
        val request = CartRequest(userId, productId, 0)

        RetrofitClient.instance.removeFromCart(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    // Xóa thành công trên Server -> Xóa khỏi list local để cập nhật UI ngay lập tức
                    cartItems.removeIf { it.product.id == productId }
                    Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Lỗi xóa: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun placeOrder(context: Context, paymentMethod: String, onSuccess: () -> Unit) {
        val userId = UserSession.currentUserId
        if (userId == null) {
            Toast.makeText(context, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show()
            return
        }


        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show()
            return
        }

        var address = UserSession.currentUserAddress
        if (address.isNullOrBlank()) {
            address = "Khách chưa cập nhật địa chỉ"
        }

        var phone = UserSession.currentUserPhone
        if (phone.isNullOrBlank()) {
            phone = "0999999999"
        }

        val orderDetails = cartItems.map { item ->
            OrderDetailRequest(
                productId = item.product.id,
                quantity = item.quantity,
                price = item.product.price
            )
        }


        val orderRequest = OrderRequest(
            userId = userId,
            address = address,
            phone = phone,
            paymentMethod = paymentMethod,
            totalPrice = getTotalPrice(),
            cartItems = orderDetails // <--- Truyền danh sách sản phẩm vào đây
        )

        RetrofitClient.instance.createOrder(orderRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {


                    cartItems.clear()

                    Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                    onSuccess()

                    // Gọi API xóa giỏ hàng trong Database (nếu Server có lưu giỏ hàng riêng)
                    RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse>{
                        override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
                        override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
                    })

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
}