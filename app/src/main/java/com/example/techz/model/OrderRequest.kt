package com.example.techz.model
//
import com.google.gson.annotations.SerializedName

data class OrderRequest(
    // App dùng 'userId', nhưng khi gửi lên Server sẽ tự đổi thành 'ma_khach_hang'
    @SerializedName("ma_khach_hang")
    val userId: Int,

    @SerializedName("dia_chi")
    val address: String,

    @SerializedName("so_dien_thoai")
    val phone: String,

    @SerializedName("phuong_thuc_thanh_toan")
    val paymentMethod: String,

    @SerializedName("tong_tien")
    val totalPrice: Double
)