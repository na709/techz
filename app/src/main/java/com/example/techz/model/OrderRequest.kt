package com.example.techz.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("ma_khach_hang")
    val userId: Int,

    @SerializedName("dia_chi")
    val address: String,

    @SerializedName("so_dien_thoai")
    val phone: String,

    @SerializedName("phuong_thuc_thanh_toan")
    val paymentMethod: String,

    @SerializedName("tong_tien")
    val totalPrice: Double,

    @SerializedName("san_pham")
    val cartItems: List<OrderDetailRequest>,

    @SerializedName("voucher_id")
    val voucherId: Int?,

    @SerializedName("discount_amount") val discountAmount: Double,

)

data class OrderDetailRequest(

    @SerializedName("id_san_pham")
    val productId: Int,

    @SerializedName("so_luong")
    val quantity: Int,

    @SerializedName("gia_ban")
    val price: Double
)