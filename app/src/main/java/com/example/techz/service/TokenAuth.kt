package com.example.techz.service

import android.content.Context
import android.util.Log
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuth(private val context: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = UserSession.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            return null
        }

        return try {
            val apiService = RetrofitClient.instance

            val call = apiService.refreshToken(mapOf("refreshToken" to refreshToken))
            val refreshResponse = call.execute()

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val body = refreshResponse.body()!!

                val newAccessToken = body["accessToken"]
                val newRefreshToken = body["refreshToken"]

                if (!newAccessToken.isNullOrEmpty()) {

                    UserSession.saveNewTokens(context, newAccessToken, newRefreshToken ?: refreshToken)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    null
                }
            } else {
                UserSession.logout(context)
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var p = response.priorResponse
        while (p != null) {
            result++
            p = p.priorResponse
        }
        return result
    }
}