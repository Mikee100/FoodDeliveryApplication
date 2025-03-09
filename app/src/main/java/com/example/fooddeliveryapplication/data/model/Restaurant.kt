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