package com.example.techz.service
//
import com.example.techz.model.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    // --- USER / AUTH ---
    @POST("api/user/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<AuthResponse>

    @POST("api/user/update")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<AuthResponse>

    @POST("api/login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    // --- PRODUCTS ---
    @GET("api/products")
    fun getListProducts(): Call<List<Product>>

    @GET("api/products/{id}")
    fun getProductDetail(@Path("id") id: Int): Call<Product>

    // --- CART ---

    // 1. Thêm vào giỏ
    @POST("api/cart/add")
    fun addToCart(@Body request: CartRequest): Call<AuthResponse>

    // 2. Lấy danh sách giỏ
    @GET("api/cart/{userId}")
    fun getCart(@Path("userId") userId: Int): Call<List<CartItem>>

    // 3. Xóa 1 món (Dùng HTTP DELETE có Body)
    @HTTP(method = "DELETE", path = "api/cart/remove", hasBody = true)
    fun removeFromCart(@Body request: CartRequest): Call<AuthResponse>

    // 4. Xóa sạch giỏ
    @DELETE("api/cart/clear/{userId}")
    fun clearCart(@Path("userId") userId: Int): Call<AuthResponse>

    // 5. Cập nhật số lượng
    @POST("api/cart/update")
    fun updateQuantity(@Body request: CartRequest): Call<AuthResponse>

    // --- ORDER ---
    @POST("api/order/add")
    fun createOrder(@Body orderRequest: OrderRequest): Call<AuthResponse>
}

// --- PHẦN BỊ THIẾU Ở BƯỚC TRƯỚC ---
object RetrofitClient {
    // IP VPS của bạn và Port 3000 (NodeJS)
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