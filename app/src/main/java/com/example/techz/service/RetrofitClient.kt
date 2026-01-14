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

    // debug logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        //LEVEL.BODY
        level = HttpLoggingInterceptor.Level.BODY
    }

    // thêm header cho auth
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val token = UserSession.token

        if (!token.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $token")
            Log.d("API_AUTH", "Đang gửi kèm Token: $token")
        }

        chain.proceed(builder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 4. Khởi tạo Retrofit
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}