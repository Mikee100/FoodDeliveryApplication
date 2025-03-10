package com.example.fooddeliveryapplication.data.model


data class Restaurant(
    val id: Int,
    val name: String,
    val email: String?,
    val location: String?,
    val description: String?,
    val image: String?,
    val user_id: Int?
)

data class Meal(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val image: String?,
    val category_name: String?
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