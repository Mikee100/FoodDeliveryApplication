package com.example.fooddeliveryapplication.data.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import android.util.Log
import retrofit2.Response
import retrofit2.http.Path

// Define the API service
interface RestaurantApiService {
    @GET("api/restaurants")
    suspend fun getRestaurants(): List<Restaurant>

    @GET("api/restaurants/{id}")
    suspend fun getRestaurantDetails(@Path("id") id: String): Restaurant

    @GET("api/restaurants/{id}/meals")
    suspend fun getRestaurantMeals(@Path("id") id: String): List<Meal>

    @GET("api/meals/{id}")
    suspend fun getMealDetails(@Path("id") id: String): Meal

    @GET("api/orders/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: String): Response<Order>
}

// Create a Retrofit instance
object RetrofitClient {
    private const val BASE_URL = "http://192.168.246.75:3000/" // Replace with your server IP
    private const val TAG = "RetrofitClient" // Tag for logging

    val instance: RestaurantApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d(TAG, "Retrofit instance created with base URL: $BASE_URL") // Log the base URL
        retrofit.create(RestaurantApiService::class.java)
    }
}