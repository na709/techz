package com.example.techz.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("ma_khach_hang")
    val userId: Int,

    @SerializedName("dia_chi")
    val address: String,

    @SerializedName("so_dien_thoai")
    val phone: String,

    @SerializedName("id_phuong_thuc")
    val id_phuong_thuc: Int,

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

data class OrderDetailResponse(
    @SerializedName("order") val order: OrderInfo,
    @SerializedName("items") val items: List<OrderDetailItem>
)

data class OrderInfo(
    @SerializedName("id_don_hang") val id_don_hang: Int,
    @SerializedName("ngay_dat_hang") val ngay_dat_hang: String,
    @SerializedName("tong_tien") val tong_tien: Double,
    @SerializedName("trang_thai") val trang_thai: String,
    @SerializedName("diachi_giao_hang") val diachi_giao_hang: String,
    @SerializedName("ghi_chu") val ghi_chu: String,
    val trang_thai_thanh_toan: String? = null
)

data class OrderDetailItem(
    @SerializedName("id_san_pham") val id_san_pham: Int,
    @SerializedName("ten_san_pham") val ten_san_pham: String,
    @SerializedName("hinh_anh") val hinh_anh: String?,
    @SerializedName("so_luong") val so_luong: Int,
    @SerializedName("gia_ban") val gia_ban: Double,
    @SerializedName("thanh_tien") val thanh_tien: Double
)


