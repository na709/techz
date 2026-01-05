package com.example.techz.model
import androidx.compose.ui.graphics.Color

data class Product(
    val id: String,
    val name: String,
    val price: String,
    val priceValue: Double,
    val imageColor: Color
)