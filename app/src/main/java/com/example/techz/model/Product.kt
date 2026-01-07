package com.example.techz.model


import com.google.gson.annotations.SerializedName

data class Product(
    // @SerializedName("tên_cột_trong_json")
    // val tên_biến_trong_kotlin: Kiểu_dữ_liệu

    @SerializedName("id_san_pham")
    val id: Int,

    @SerializedName("ten_san_pham")
    val name: String,

    @SerializedName("gia")
    val price: Double,

    @SerializedName("hinh_anh")
    val image: String?,

    @SerializedName("mo_ta")
    val description: String?,

    @SerializedName("loai_linh_kien")
    val category: String?
)