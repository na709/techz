package com.example.techz.model
import com.google.gson.annotations.SerializedName

data class CartRequest(
    @SerializedName("id_khach_hang")
    val userId: Int,

    @SerializedName("id_san_pham")
    val productId: Int,

    @SerializedName("so_luong")
    val quantity: Int
)

data class CartItem(
    @SerializedName("product")
    val product: Product,

    @SerializedName("quantity")
    var quantity: Int
)