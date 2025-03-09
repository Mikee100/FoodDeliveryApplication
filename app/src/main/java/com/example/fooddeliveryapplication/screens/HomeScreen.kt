package com.example.fooddeliveryapplication.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fooddeliveryapplication.data.model.Restaurant
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val tag = "HomeScreen"

    // Fetch restaurants when the composable is first launched
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                Log.d(tag, "Fetching restaurants from API...")
                val response = RetrofitClient.instance.getRestaurants()
                Log.d(tag, "Restaurants received: ${response.size} items")
                Log.d(tag, "Restaurants data: $response")
                restaurants = response
            } catch (e: Exception) {
                Log.e(tag, "Error fetching restaurants: ${e.message}")
            }
        }
    }

    // Display the restaurants
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                onClick = {
                    // Navigate to the restaurant details screen
                    navController.navigate("restaurantDetails/${restaurant.id}")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}