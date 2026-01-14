package com.example.techz.model

import com.google.gson.annotations.SerializedName
//
data class Product(
    @SerializedName("id_san_pham")
    val id: Int,

    @SerializedName("ten_san_pham")
    val name: String,

    @SerializedName("gia")
    val price: Double,

    @SerializedName(value = "hinh_anh_full", alternate = ["hinh_anh", "image"])
    val image: String?,

    @SerializedName("mo_ta")
    val description: String?,

    @SerializedName("loai_linh_kien")
    val category: String?,

    @SerializedName("so_luong_ton")
    val stock: Int
)

