package com.example.techz.service

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UserSession {
    var currentUserName by mutableStateOf<String?>(null)
        private set
    var currentUserRole by mutableStateOf<String?>(null)
        private set
    var currentUserId by mutableStateOf<Int?>(null)
        private set

<<<<<<< HEAD

    fun login(context: Context, name: String,role: String) {
=======
    fun login(context: Context, name: String) {
>>>>>>> 090d5ede692724b264fc3481132fb3fbaa7617aa
        currentUserName = name
        currentUserRole= role

        // Lưu vào SharedPreferences để giữ đăng nhập khi tắt app
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_NAME", name)
            putString("USER_ROLE", role)
            apply()
        }
    }

    // Hàm gọi khi logout
    fun logout(context: Context) {
        currentUserName = null
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("USER_NAME")
            apply()
        }
    }

}