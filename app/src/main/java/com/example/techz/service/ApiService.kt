package com.example.techz.service


import com.example.techz.model.AuthResponse
import com.example.techz.model.CartItem
import com.example.techz.model.CartRequest
import com.example.techz.model.ChangePasswordRequest
import com.example.techz.model.CreateManagerRequest
import com.example.techz.model.LoginRequest
import com.example.techz.model.OrderRequest
import com.example.techz.model.Product
import com.example.techz.model.RegisterRequest
import com.example.techz.model.UpdateProfileRequest
import com.example.techz.model.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/order/detail/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: Int): OrderDetailResponse

    @POST("api/payment/momo/create")
    fun createMomoPayment(@Body request: MomoPaymentRequest): Call<MomoResponse>

    @GET("api/vouchers")
    fun getAvailableVouchers(): Call<List<Voucher>>

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
    @POST("api/cart/remove")
    fun removeFromCart(@Body request: CartRequest): Call<AuthResponse>
    @POST("api/cart/update")
    fun updateQuantity(@Body request: CartRequest): Call<AuthResponse>

    @GET("api/cart/{userId}")
    fun getCart(@Path("userId") userId: Int): Call<List<CartItem>>


    @DELETE("api/cart/clear/{userId}")
    fun clearCart(@Path("userId") userId: Int): Call<AuthResponse>
    @POST("api/order/add")
    fun createOrder(@Body orderRequest: OrderRequest): Call<AuthResponse>

    // lấy loại linh kiện
    @GET("/api/categories")
    fun getCategories(): Call<List<String>>

    //lọc theo loại
    @GET("/api/products/category/{type}")
    fun filterByCategory(@Path("type") type: String): Call<List<Product>>

    //tìm kiếm + debounce
    @GET("/api/search")
    fun searchByName(@Query("q") query: String): Call<List<Product>>
    @POST("api/admin/voucher/add")
    fun addVoucher(@Body request: VoucherRequest): Call<AuthResponse>

}





