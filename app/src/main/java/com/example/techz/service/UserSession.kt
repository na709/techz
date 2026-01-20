package com.example.techz.service
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.techz.model.User
import android.util.Base64
import android.util.Log
import android.widget.Toast
import org.json.JSONObject

object UserSession {
    var token by mutableStateOf<String?>(null)
        public set
    var refreshToken by mutableStateOf<String?>(null)
        private set
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
        get() = currentUserId != null && token != null

    fun login(context: Context, user: User, role: String, accessToken: String?, newRefreshToken: String?) {
        currentUserId = user.id
        currentUserName = user.name
        currentUserRole = role
        currentUserAddress = user.address
        currentUserPhone = user.phone
        currentUserEmail = user.email
        token = accessToken
        refreshToken = newRefreshToken

        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("USER_ID", user.id)
            putString("USER_NAME", user.name ?: "")
            putString("USER_EMAIL", user.email ?: "")
            putString("USER_ROLE", role)
            putString("USER_PHONE", user.phone ?: "")
            putString("USER_ADDRESS", user.address ?: "")
            putString("ACCESS_TOKEN", accessToken)
            putString("REFRESH_TOKEN", newRefreshToken)
            apply()
        }
    }

    fun saveNewTokens(context: Context, newAccessToken: String, newRefreshToken: String?) {
        token = newAccessToken
        if (newRefreshToken != null) {
            refreshToken = newRefreshToken
        }

        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("ACCESS_TOKEN", token)
            if (newRefreshToken != null) {
                putString("REFRESH_TOKEN", refreshToken)
            }
            apply()
        }
    }

    fun logout(context: Context, forceClear: Boolean = false) {
        if (forceClear) {
            clearLocalData(context)
            return
        }

        val tokenToRevoke = refreshToken
        if (!tokenToRevoke.isNullOrEmpty()) {
            try {
                RetrofitClient.instance.logout(mapOf("refreshToken" to tokenToRevoke))
                    .enqueue(object : retrofit2.Callback<Void> {
                        override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                            clearLocalData(context)
                        }
                        override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                            clearLocalData(context)
                        }
                    })
            } catch (e: Exception) {
                clearLocalData(context)
            }
        } else {
            clearLocalData(context)
        }
    }

    private fun clearLocalData(context: Context) {
        currentUserId = null
        currentUserName = null
        currentUserRole = null
        currentUserPhone = null
        currentUserAddress = null
        token = null
        refreshToken = null
        val sharedPref = context.getSharedPreferences("TechZ_Prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
    }
    //gọi khi update
    fun updateSession(context: Context, name: String, phone: String, address: String) {
        currentUserName = name
        currentUserPhone = phone
        currentUserAddress = address

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
        val savedToken = sharedPref.getString("ACCESS_TOKEN", null)
        val savedRefreshToken = sharedPref.getString("REFRESH_TOKEN", null)

        if (savedToken != null) {
            val savedId = sharedPref.getInt("USER_ID", -1)
            if (savedId != -1) {
                currentUserId = savedId
                token = savedToken
                refreshToken = savedRefreshToken
                currentUserName = sharedPref.getString("USER_NAME", null)
                currentUserRole = sharedPref.getString("USER_ROLE", "user")
                currentUserPhone = sharedPref.getString("USER_PHONE", "")
                currentUserAddress = sharedPref.getString("USER_ADDRESS", "")
            }
        }
    }
    private fun isTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size < 2) return true

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonObject = JSONObject(payload)

            if (jsonObject.has("exp")) {
                val exp = jsonObject.getLong("exp")
                val now = System.currentTimeMillis() / 1000
                return now > exp
            }
        } catch (e: Exception) {
            return true
        }
        return false
    }
}