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
            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                // debug lỗi
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
                Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show()
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

        // Xóa UI ngay
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
    // 4. Đặt hàng (Đã Merge logic chuẩn)
    // ------------------------------------------------------------------
    fun placeOrder(
        context: Context,
        //new
        selectedItems: List<CartItem>, // <--- NHẬN DANH SÁCH MÓN ĐÃ CHỌN
        //-----
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
//        if (cartItems.isEmpty()) {
//            Toast.makeText(context, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show()
//            return
//        }
        if (selectedItems.isEmpty()) { // SỬA: Chỉ check danh sách chọn
            Toast.makeText(context, "Chưa chọn sản phẩm nào!", Toast.LENGTH_SHORT).show()
            return
        }

        val address = UserSession.currentUserAddress
        val phone = UserSession.currentUserPhone

        if (address.isNullOrBlank() || phone.isNullOrBlank()) {
            Toast.makeText(context, "Vui lòng cập nhật Địa chỉ và SĐT!", Toast.LENGTH_LONG).show()
            return
        }

        // 1. Tính toán giá tiền
        //sửa---
        val rawTotal = selectedItems.sumOf { it.product.price * it.quantity } // SỬA: Chỉ tính món chọn
        //------
        val finalPrice = (rawTotal - discount).coerceAtLeast(0.0)

        // 2. Map dữ liệu cart sang model OrderDetailRequest
//        val listOrderDetails = cartItems.map { item ->
//            OrderDetailRequest(
//                productId = item.product.id,
//                quantity = item.quantity,
//                price = item.product.price
//            )
//        }
        val listOrderDetails = selectedItems.map { item -> // SỬA: Chỉ map món chọn
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
                        // Truyền thêm selectedItems vào để xử lý sau khi thanh toán xong
                        initiateMomoPayment(context, orderId, finalPrice.toLong(),selectedItems, onSuccess)
                    } else {
                        // Nếu là Tiền mặt (COD) hoặc ATM thường -> Thành công luôn
                        handleOrderSuccess(userId, context,selectedItems, onSuccess)
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

    // --- CÁC HÀM PHỤ TRỢ (HELPER FUNCTIONS) ---

    // Xử lý khi đặt hàng thành công (Xóa giỏ hàng UI & Server, thông báo)
//    private fun handleOrderSuccess(userId: Int, context: Context, onSuccess: () -> Unit) {
//        cartItems.clear() // Xóa UI
//        clearServerCart(userId) // Xóa Database Server
//        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
//        onSuccess() // Callback điều hướng về Home/Success
//    }
    private fun handleOrderSuccess(
        userId: Int,
        context: Context,
        purchasedItems: List<CartItem>, // <--- MỚI
        onSuccess: () -> Unit
    ) {
        // 1. Xóa khỏi giao diện (Chỉ xóa món đã mua)
        val purchasedIds = purchasedItems.map { it.product.id }.toSet()
        cartItems.removeIf { it.product.id in purchasedIds }

        // 2. Xóa trên Server
        // Vì API clearCart xóa tất cả, nên ta cần xóa từng món (hoặc sửa API Server).
        // Cách an toàn hiện tại: Loop xóa từng món đã mua.
        purchasedItems.forEach { item ->
            RetrofitClient.instance.removeFromCart(CartRequest(userId, item.product.id, 0)).enqueue(object : Callback<AuthResponse>{
                override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
                override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
            })
        }

        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
        onSuccess()
    }

    // Gọi API lấy link thanh toán Momo
    private fun initiateMomoPayment(
        context: Context,
        orderId: Int,
        amount: Long,
        purchasedItems: List<CartItem>,//Tham số mới được thêm vào
        onSuccess: () -> Unit) {
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
                        //new
                        if (userId != null) handleOrderSuccess(userId, context, purchasedItems, onSuccess)
                        //---
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

    // Xóa sạch giỏ hàng trên Server
    private fun clearServerCart(userId: Int) {
        RetrofitClient.instance.clearCart(userId).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {}
            override fun onFailure(c: Call<AuthResponse>, t: Throwable) {}
        })
    }
}