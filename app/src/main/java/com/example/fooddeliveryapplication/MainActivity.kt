package com.example.fooddeliveryapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fooddeliveryapplication.screens.HomeScreen
import com.example.fooddeliveryapplication.screens.MealDetailsScreen
import com.example.fooddeliveryapplication.screens.OrderStatusScreen
import com.example.fooddeliveryapplication.screens.RestaurantCard
import com.example.fooddeliveryapplication.screens.RestaurantDetailsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Set up navigation
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home" // Start with the HomeScreen
                    ) {
                        // Define the HomeScreen route
                        composable("home") {
                            HomeScreen(navController = navController)
                        }

                        // Define the RestaurantDetailsScreen route
                        composable("restaurantDetails/{restaurantId}") { backStackEntry ->
                            val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                            RestaurantDetailsScreen(restaurantId = restaurantId, navController = navController)
                        }
                        composable("orderStatus/{orderId}") { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId")
                            OrderStatusScreen(orderId = orderId , navController=navController)
                        }

                        composable("mealDetails/{mealId}") { backStackEntry ->
                            val mealId = backStackEntry.arguments?.getString("mealId")
                            MealDetailsScreen(navController = navController, mealId = mealId)
                        }
                    }
                }
            }
        }
    }
}


