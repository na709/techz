package com.example.techz.service

import com.example.techz.model.AuthResponse
import com.example.techz.model.ChangePasswordRequest
import com.example.techz.model.CreateManagerRequest
import com.example.techz.model.LoginRequest
import com.example.techz.model.Product
import com.example.techz.model.RegisterRequest
import com.example.techz.model.UpdateProfileRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/user/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<AuthResponse>
    @POST("api/user/update")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<AuthResponse>

    @POST("api/login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    // get list products
    @GET("api/products")
    fun getListProducts(): Call<List<Product>>

    // get productdetail
    @GET("api/products/{id}")
    fun getProductDetail(@Path("id")id: Int): Call<Product>
}

object RetrofitClient {
    private const val BASE_URL = "http://160.250.247.5:3000/"
    //private const val BASE_URL = "http://10.0.2.2:3000/"
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}