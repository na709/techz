package com.example.techz.model

import com.google.gson.annotations.SerializedName


/*data class RegisterRequest(
    val name: String,
    val email: String,
    val username: String,
    val password: String
)*/

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("address") val address: String?
)

// 2. Model chung cho Response (Vì cả Login và Register đều trả về cấu trúc giống nhau)
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("role") val role: String,
    @SerializedName("user") val user: User? // Có thể null
)

// 3. Body gửi lên khi Login
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// body post lên server khi register
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String


)
//body gửi lên khi thay đổi pass
data class ChangePasswordRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

// 4. Body gửi lên khi Tạo tài khoản Quản lý (Register)
data class CreateManagerRequest(
    @SerializedName("current_admin_id") val currentAdminId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)
//đẩy data lên để update thông tin
data class UpdateProfileRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String
)

