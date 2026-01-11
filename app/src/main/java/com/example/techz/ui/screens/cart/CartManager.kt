package com.example.techz.ui.screens.cart

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.techz.model.AuthResponse
import com.example.techz.model.CartItem
import com.example.techz.model.CartRequest
import com.example.techz.model.OrderRequest
import com.example.techz.model.Product
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CartManager {
    private const val PREF_NAME = "TechZ_Cart"
    private const val CART_KEY = "cart_list"

    // Danh sách giỏ hàng để hiển thị lên màn hình
    val cartItems = mutableStateListOf<CartItem>()

    // Hàm tính tổng tiền
    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    // 1. HÀM LOAD CART (Tải giỏ hàng từ Server hoặc Local)
    fun loadCart(context: Context) {
        val userId = UserSession.currentUserId
        if (userId != null) {
            // Đã đăng nhập -> Tải từ Server
            RetrofitClient.instance.getCart(userId).enqueue(object : Callback<List<CartItem>> {
                override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                    if (response.isSuccessful) {
                        cartItems.clear()
                        cartItems.addAll(response.body() ?: emptyList())
                    }
                }
                override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                    // Nếu lỗi mạng thì thử load local
                    loadLocal(context)
                }
            })
        } else {
            // Chưa đăng nhập -> Tải từ bộ nhớ máy
            loadLocal(context)
        }
    }

    // 2. HÀM ADD TO CART (Thêm vào giỏ)
    fun addToCart(context: Context, product: Product) {
        val userId = UserSession.currentUserId

        // Cập nhật giao diện ngay lập tức (UI Optimistic Update)
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            cartItems.add(CartItem(product, 1))
        }

        // Lưu xuống bộ nhớ máy (đề phòng chưa có mạng)
        saveLocal(context)

        // Nếu đã đăng nhập thì gửi lên Server
        if (userId != null) {
            val request = CartRequest(
                id_khach_hang = userId,
                id_san_pham = product.id,
                so_luong = 1
            )
            Log.d("CartAPI", "Adding to cart: $request")
            RetrofitClient.instance.addToCart(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (!response.isSuccessful) Log.e("CartAPI", "Error adding: ${response.code()}")
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Log.e("CartAPI", "Fail: ${t.message}")
                }
            })
        }
    }

    // 3. HÀM UPDATE QUANTITY (Tăng giảm số lượng)
    fun updateQuantity(context: Context, productId: Int, change: Int) {
        val item = cartItems.find { it.product.id == productId }
        item?.let {
            if (it.quantity + change > 0) {
                it.quantity += change
            } else {
                cartItems.remove(it)
                // Nếu muốn xóa hẳn khỏi server thì gọi API remove ở đây (tùy chọn)
            }
            saveLocal(context)
        }
    }

    // 4. HÀM PLACE ORDER (Thanh toán & Đặt hàng)
    fun placeOrder(context: Context, paymentMethod: String, onSuccess: () -> Unit) {
        val userId = UserSession.currentUserId

        // [FIX LỖI 400] Kiểm tra cả null và rỗng ""
        val rawAddress = UserSession.currentUserAddress
        val address = if (rawAddress.isNullOrEmpty()) "Địa chỉ mặc định (Chưa cập nhật)" else rawAddress

        val rawPhone = UserSession.currentUserPhone
        val phone = if (rawPhone.isNullOrEmpty()) "0000000000" else rawPhone

        val totalPrice = getTotalPrice()

        if (userId != null) {
            val request = OrderRequest(
                ma_khach_hang = userId,
                dia_chi = address,
                so_dien_thoai = phone,
                phuong_thuc_thanh_toan = paymentMethod,
                tong_tien = totalPrice
            )

            Log.d("API_ORDER", "Dữ liệu gửi đi: $request") // Soi Log xem gửi gì

            RetrofitClient.instance.createOrder(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        clearCart(context)
                        onSuccess()
                    } else {
                        // [QUAN TRỌNG] In lỗi từ Server ra Logcat để đọc
                        val errorMsg = response.errorBody()?.string() ?: "Lỗi không xác định"
                        Log.e("API_ORDER", "Lỗi 400 chi tiết: $errorMsg")
                        Toast.makeText(context, "Lỗi đặt hàng: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(context, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "Lỗi: User ID bị null", Toast.LENGTH_SHORT).show()
        }
    }

    // 5. HÀM CLEAR CART (Xóa giỏ hàng sau khi mua xong)
    fun clearCart(context: Context) {
        val userId = UserSession.currentUserId
        cartItems.clear()
        saveLocal(context)
        if (userId != null) {
            RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {}
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {}
            })
        }
    }

    // --- CÁC HÀM HỖ TRỢ LƯU LOCAL (SharedPreferences) ---
    private fun saveLocal(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(cartItems)
        sharedPref.edit().putString(CART_KEY, json).apply()
    }

    private fun loadLocal(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sharedPref.getString(CART_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            val items: List<CartItem> = Gson().fromJson(json, type)
            cartItems.clear()
            cartItems.addAll(items)
        }
    }
}