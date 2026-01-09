package com.example.techz.model

import com.google.gson.annotations.SerializedName


data class RegisterRequest(
    val name: String,
    val email: String,
    val username: String,
    val password: String
)
// 1. User (Object con bên trong response)
data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
)

// 2. Model chung cho Response (Vì cả Login và Register đều trả về cấu trúc giống nhau)
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User? // Có thể null nếu thất bại
)

// 3. Body gửi lên khi Login
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// 4. Body gửi lên khi Tạo tài khoản Quản lý (Register)
data class CreateManagerRequest(
    @SerializedName("current_admin_id") val currentAdminId: Int, // ID admin hiện tại
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)