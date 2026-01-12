package com.example.techz.model
///
import com.google.gson.annotations.SerializedName

// Request gửi lên khi Thêm/Sửa/Xóa giỏ hàng
data class CartRequest(
    @SerializedName("id_khach_hang") // Map sang tên cột trong DB
    val userId: Int,

    @SerializedName("id_san_pham")
    val productId: Int,

    @SerializedName("so_luong")
    val quantity: Int
)

// Item hiển thị trong danh sách giỏ hàng
data class CartItem(
    @SerializedName("product")
    val product: Product, // Đảm bảo class Product của bạn có các trường id, name, price, image

    @SerializedName("quantity")
    var quantity: Int
)