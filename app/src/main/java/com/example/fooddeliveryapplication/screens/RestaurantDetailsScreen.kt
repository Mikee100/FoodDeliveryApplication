package com.example.fooddeliveryapplication.screens
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.fooddeliveryapplication.data.model.Meal
import com.example.fooddeliveryapplication.data.model.Restaurant
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@Composable
fun RestaurantDetailsScreen(restaurantId: String?, navController: NavController) {
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch restaurant details and meals
    LaunchedEffect(restaurantId) {
        coroutineScope.launch {
            try {
                // Fetch restaurant details
                val restaurantResponse = RetrofitClient.instance.getRestaurantDetails(restaurantId ?: "")
                restaurant = restaurantResponse

                // Fetch meals for the restaurant
                val mealsResponse = RetrofitClient.instance.getRestaurantMeals(restaurantId ?: "")
                meals = mealsResponse
            } catch (e: Exception) {
                Log.e("RestaurantDetailsScreen", "Error fetching details: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        // Show a loading indicator
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (restaurant == null) {
        // Show an error message if no restaurant is found
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Restaurant details not available.", color = MaterialTheme.colorScheme.error)
        }
    } else {
        // Display restaurant details and meals
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {

                // Restaurant Image
                if (!restaurant?.image.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = restaurant?.image),
                        contentDescription = restaurant?.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Restaurant Name
                Text(
                    text = restaurant?.name ?: "",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                // Restaurant Location
                if (!restaurant?.location.isNullOrEmpty()) {
                    Text(
                        text = restaurant?.location ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Restaurant Description
                if (!restaurant?.description.isNullOrEmpty()) {
                    Text(
                        text = restaurant?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Display meals
            items(meals) { meal ->
                MealItem(meal = meal, navController = navController)
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}

@Composable
fun MealItem(meal: Meal, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate("mealDetails/${meal.id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Meal Image
            if (!meal.image.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = meal.image),
                    contentDescription = meal.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Meal Name
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Meal Description
            if (!meal.description.isNullOrEmpty()) {
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Meal Price
            Text(
                text = "Ksh ${meal.price}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
