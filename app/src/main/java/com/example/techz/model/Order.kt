package com.example.techz.model
import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("id_don_hang") val id: Int,
    @SerializedName("ngay_dat_hang") val date: String,
    @SerializedName("tong_tien") val totalPrice: Int,
    @SerializedName("trang_thai") val status: String,
    @SerializedName("ten_san_pham") val productName: String?,
    @SerializedName("so_luong_sp") val quantity: Int,
    @SerializedName("trang_thai_thanh_toan") val paymentStatus: String?
)

data class OrderActionRequest(
    @SerializedName("id_don_hang") val orderId: Int
)