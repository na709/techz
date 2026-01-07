package com.example.techz.service

import com.example.techz.model.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Path

interface ApiService {

    // get list products
    @GET("api/products")
    fun getListProducts(): Call<List<Product>>

    // get productdetail
    @GET("api/products/{id}")
    fun getProductDetail(@Path("id")id: Int): Call<Product>
}

object RetrofitClient {
    private const val BASE_URL = "http://160.250.247.5:4567/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}