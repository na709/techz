package com.example.techz.model

import com.google.gson.annotations.SerializedName

data class VoucherRequest(
    @SerializedName("ma_code") val code: String,
    @SerializedName("ten_chuong_trinh") val name: String,
    @SerializedName("phan_tram_giam") val discountPercent: Int,
    @SerializedName("so_tien_giam_toi_da") val maxDiscountAmount: Double,
    @SerializedName("don_hang_toi_thieu") val minOrderValue: Double,
    @SerializedName("so_luong") val quantity: Int,
    @SerializedName("ngay_bat_dau") val startDate: String,
    @SerializedName("ngay_ket_thuc") val endDate: String
)

data class Voucher(
    @SerializedName("id_khuyen_mai") val id: Int,
    @SerializedName("ma_code") val code: String,
    @SerializedName("ten_chuong_trinh") val name: String,
    @SerializedName("phan_tram_giam") val percent: Int,
    @SerializedName("so_tien_giam_toi_da") val maxDiscount: Double,
    @SerializedName("don_hang_toi_thieu") val minOrder: Double,
    @SerializedName("ngay_ket_thuc") val endDate: String
)