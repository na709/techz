package com.example.techz.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.widget.Toast
import com.example.techz.MainActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.os.Handler

@SuppressLint("StaticFieldLeak")
object RetrofitClient {
    private const val BASE_URL = "https://dvna.site/"

    //private const val BASE_URL = "http://103.228.36.78:3000/"
    private var apiService: ApiService? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                val token = UserSession.token
                if (!token.isNullOrEmpty()) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }

            val logoutInterceptor = Interceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)

                if (response.code == 403) {
                    val errorBody = response.peekBody(Long.MAX_VALUE).string()
                    var serverMessage = "Phiên đăng nhập hết hạn!"
                    try {
                        val jsonObject = JSONObject(errorBody)
                        if (jsonObject.has("message")) {
                            serverMessage = jsonObject.getString("message")
                        }
                    } catch (e: Exception) { e.printStackTrace() }

                    appContext?.let { ctx ->
                         Handler(Looper.getMainLooper()).post {
                            UserSession.logout(ctx, forceClear = true)
                            Toast.makeText(ctx, serverMessage, Toast.LENGTH_LONG).show()
                            val intent = Intent(ctx, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            ctx.startActivity(intent)
                        }
                    }
                }
                response
            }

            val authenticator = TokenAuth(appContext!!)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .addInterceptor(logoutInterceptor)
                .authenticator(authenticator)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }

    val instance: ApiService
        get() {
            if (apiService == null) {
                throw IllegalStateException("RetrofitClient null! Hãy gọi init().")
            }
            return apiService!!
        }
}