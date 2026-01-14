package com.example.techz.service

import android.util.Log
import com.example.techz.service.UserSession
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {


    private const val BASE_URL = "https://dvna.site/"
    //private const val BASE_URL = "http://103.228.36.78:3000/"

    // 1. Cấu hình Logging (Để xem API gửi gì, nhận gì trong Logcat)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Chọn LEVEL.BODY để xem cả nội dung JSON trả về (Rất quan trọng để debug lỗi)
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. Cấu hình Auth Interceptor (Tự động thêm Token vào Header)
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // Lấy token từ UserSession
        val token = UserSession.token

        // Nếu có token thì thêm vào Header
        if (!token.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $token")
            Log.d("API_AUTH", "Đang gửi kèm Token: $token")
        }

        chain.proceed(builder.build())
    }

    // 3. Cấu hình OkHttpClient (Kết hợp 2 cái trên)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Gắn logging
        .addInterceptor(authInterceptor)    // Gắn tự động thêm Token
        .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
        .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
        .build()

    // 4. Khởi tạo Retrofit
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <--- Quan trọng: Phải gắn client vào đây
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}