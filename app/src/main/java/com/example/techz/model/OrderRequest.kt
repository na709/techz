package com.example.techz.model

data class OrderRequest(
    val ma_khach_hang: Int,
    val dia_chi: String,
    val so_dien_thoai: String,
    val phuong_thuc_thanh_toan: String,
    val tong_tien: Double
)