package com.example.techz.service

import android.content.Context
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuth(private val context: Context) : Authenticator {
    var BASE_URL = "http://103.228.36.78:3000/"

    override fun authenticate(route: Route?, response: Response): Request? {

        synchronized(this) {

            val currentToken = UserSession.token
            val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")
            if (currentToken != null && requestToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            if (responseCount(response) >= 2) {
                return null
            }

            val refreshToken = UserSession.refreshToken
            if (refreshToken.isNullOrEmpty()) {
                return null
            }
            return try {
                val tempClient = OkHttpClient.Builder().build()
                val tempRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(tempClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val tempApiService = tempRetrofit.create(ApiService::class.java)

                val call = tempApiService.refreshToken(mapOf("refreshToken" to refreshToken))
                val refreshResponse = call.execute()

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val authBody = refreshResponse.body()!!
                    val newAccessToken = authBody.accessToken
                    val newRefreshToken = authBody.refreshToken

                    if (!newAccessToken.isNullOrEmpty()) {
                        UserSession.saveNewTokens(context, newAccessToken, newRefreshToken ?: refreshToken)

                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    } else {
                        null
                    }
                } else {
                    //debug
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
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