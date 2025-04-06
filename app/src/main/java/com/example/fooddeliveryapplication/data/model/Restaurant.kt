package com.example.fooddeliveryapplication.data.model

import com.google.gson.annotations.SerializedName

data class Restaurant(
    val id: Int,
    val name: String,
    val email: String?,
    val location: String?,
    val description: String?,
    val image: String?,
    val user_id: Int?,
    val cuisineType: String? = null,
    val rating: Float? = null,
    val deliveryTime: Int? = null,
    val deliveryFee: Int? = null
)
data class MealResponse(
    val success: Boolean,
    val data: Meal
)


data class Meal(
    val id: String?,  // Change to nullable
    val name: String?,
    val description: String?,
    val price: Double,
    val image: String?,
    val categoryName: String?,
    val restaurantName: String?
)
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("message") val message: String?
)
data class Order(
    val id: Int,
    val status: String, // e.g., "Order Placed", "In the Kitchen", "Out for Delivery", "Delivered"
    val items: List<OrderItem>, // List of items in the order
    val totalPrice: Double,
    val userId: Int,
    val createdAt: String, // Timestamp of when the order was created
    val updatedAt: String? // Timestamp of when the order was last updated
)

data class OrderItem(
    val mealId: String,
    val quantity: Int,
    val price: Double
)
data class OrderResponse(
    val orderId: Int
)
// Define OrderRequest data class
data class OrderRequest(
    val mealId: String,
    val specifics: String,
    val phoneNumber: String,
    val price: Double
)
data class Location(
    val lat: Double,
    val lng: Double
)
data class MpesaPaymentRequest(
    val phoneNumber: String,
    val amount: Double
)

data class MpesaPaymentResponse(
    val merchantRequestId: String,
    val checkoutRequestId: String,
    val responseCode: String,
    val responseDescription: String,
    val customerMessage: String
)
