package com.example.techz.service

import com.example.techz.model.AuthResponse
import com.example.techz.model.CartItem
import com.example.techz.model.CartRequest
import com.example.techz.model.ChangePasswordRequest
import com.example.techz.model.CreateManagerRequest
import com.example.techz.model.LoginRequest
import com.example.techz.model.Order
import com.example.techz.model.OrderRequest
import com.example.techz.model.Product
import com.example.techz.model.RegisterRequest
import com.example.techz.model.UpdateProfileRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("api/user/{id}/orders")
    fun getUserOrders(@Path("id") userId: Int): Call<List<Order>>

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
    //các thao tác với giỏ hàng
    @POST("api/cart/add")
    fun addToCart(@Body request: CartRequest): Call<AuthResponse>

    @GET("api/cart/{userId}")
    fun getCart(@Path("userId") userId: Int): Call<List<CartItem>>

    @HTTP(method = "DELETE", path = "api/cart/remove", hasBody = true)
    fun removeFromCart(@Body request: CartRequest): Call<AuthResponse>

    @DELETE("api/cart/clear/{userId}")
    fun clearCart(@Path("userId") userId: Int): Call<AuthResponse>
    @POST("api/order/add")
    fun createOrder(@Body orderRequest: OrderRequest): Call<AuthResponse>

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