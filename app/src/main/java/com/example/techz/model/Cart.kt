package com.example.techz.model
///
import com.google.gson.annotations.SerializedName

// Request gửi lên khi Thêm/Sửa/Xóa giỏ hàng
data class CartRequest(
    @SerializedName("id_khach_hang")
    val userId: Int,

    @SerializedName("id_san_pham")
    val productId: Int,

    @SerializedName("so_luong")
    val quantity: Int
)
//
// Item hiển thị trong danh sách giỏ hàng
data class CartItem(
    @SerializedName("product")
    val product: Product,

    @SerializedName("quantity")
    var quantity: Int
)