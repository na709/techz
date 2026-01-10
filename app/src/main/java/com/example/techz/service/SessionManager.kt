package com.example.techz.service

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.techz.model.User

object UserSession {
    var currentUserName by mutableStateOf<String?>(null)
        private set
    var currentUserEmail by mutableStateOf<String?>(null)
        private set
    var currentUserRole by mutableStateOf<String?>(null)
        private set
    var currentUserId by mutableStateOf<Int?>(null)
        private set
    var currentUserPhone by mutableStateOf<String?>(null)
        private set
    var currentUserAddress by mutableStateOf<String?>(null)
        private set


    val isAdmin: Boolean
        get() = currentUserRole == "admin"
    val isLoggedIn: Boolean
        get() = currentUserId != null

    fun login(context: Context, user: User, role: String) {
        currentUserId = user.id
        currentUserName = user.name
        currentUserRole = role
        currentUserAddress = user.address
        currentUserPhone = user.phone
        currentUserEmail = user.email


        // Lưu cục bộ để giữ đăng nhập khi tắt app
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("USER_ID", user.id)
            putString("USER_NAME", user.name ?: "")
            putString("USER_EMAIL", user.email ?: "")
            putString("USER_ROLE", role)
            putString("USER_PHONE", user.phone ?: "")
            putString("USER_ADDRESS", user.address ?: "")
            apply()
        }
    }

    // gọi khi logout
    fun logout(context: Context) {
        currentUserId = null
        currentUserName = null
        currentUserRole = null
        currentUserPhone = null
        currentUserAddress = null
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            //remove("USER_NAME")
            clear()
            apply()
        }
    }
    //gọi khi update
    fun updateSession(context: Context, name: String, phone: String, address: String) {
        currentUserName = name
        currentUserPhone = phone
        currentUserAddress = address

        // Cập nhật vào ổ cứng (SharedPreferences)
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_NAME", name)
            putString("USER_PHONE", phone)
            putString("USER_ADDRESS", address)
            apply()
        }
    }

    fun initSession(context: Context) {
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)

        val savedId = sharedPref.getInt("USER_ID", -1)

        if (savedId != -1) {
            currentUserId = savedId
            currentUserName = sharedPref.getString("USER_NAME", null)
            currentUserRole = sharedPref.getString("USER_ROLE", "user")
            currentUserPhone = sharedPref.getString("USER_PHONE", "")
            currentUserAddress = sharedPref.getString("USER_ADDRESS", "")
        }
    }

}