@file:Suppress("DEPRECATION")

package com.example.fooddeliveryapplication.screens
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.fooddeliveryapplication.data.model.Meal
import com.example.fooddeliveryapplication.data.model.Restaurant
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import coil.compose.AsyncImage


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(restaurantId: String?, navController: NavController) {
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    var meals by remember { mutableStateOf<Map<String, List<Meal>>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    // Fetch data
    LaunchedEffect(restaurantId) {
        coroutineScope.launch {
            try {
                val restaurantResponse = RetrofitClient.instance.getRestaurantDetails(restaurantId ?: "")
                restaurant = restaurantResponse

                val mealsResponse = RetrofitClient.instance.getRestaurantMeals(restaurantId ?: "")
                meals = groupMealsByCategory(mealsResponse)
            } catch (e: Exception) {
                error = "Failed to load data: ${e.message}"
                Log.e("RestaurantDetails", "Error: ${e.stackTraceToString()}")
            } finally {
                loading = false
            }
        }
    }

    // UI States
    if (loading) {
        FullScreenLoading()
    } else if (error != null) {
        FullScreenError(error!!) { coroutineScope.launch { loading = true; error = null } }
    } else if (restaurant == null) {
        FullScreenError("Restaurant not found") { navController.popBackStack() }
    } else {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                OrderSummarySheet(restaurant!!, meals.flatMap { it.value })
            },
            sheetPeekHeight = 80.dp,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetShadowElevation = 16.dp
        ) { padding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Restaurant Header
                item {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            AsyncImage(
                                model = restaurant?.image,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            GradientOverlay()
                            RestaurantHeader(restaurant!!)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Restaurant Info
                        RestaurantInfoSection(restaurant!!)

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Categories Filter
                item {
                    val categories = listOf("All") + meals.keys.toList()
                    ScrollableCategories(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Meals List
                meals.forEach { (category, mealsList) ->
                    if (selectedCategory == "All" || selectedCategory == category) {
                        item {
                            CategoryHeader(category, mealsList.size)
                        }

                        items(mealsList) { meal ->
                            MealCard(
                                meal = meal,
                                onItemClick = { navController.navigate("mealDetails/${meal.id}") },
                                onAddToCart = { /* Handle add to cart */ }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// Components

@Composable
private fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading restaurant details...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FullScreenError(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun GradientOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                    startY = 0f,
                    endY = 300f
                )
            )
    )
}

@Composable
private fun RestaurantHeader(restaurant: Restaurant) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)

    ) {
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "4.8 (500+)",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = "Delivery Time",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "20-30 min",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}



@Composable
private fun RestaurantInfoSection(restaurant: Restaurant) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Description
        Text(
            text = restaurant.description ?: "No description available",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Divider
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        // Additional Info
        RestaurantInfoRow(
            icon = Icons.Filled.LocationOn,
            title = "Address",
            value = restaurant.location ?: "Not specified"
        )

        RestaurantInfoRow(
            icon = Icons.Filled.Phone,
            title = "Contact",
            value = "+1 234 567 8900" // Replace with actual contact
        )

        RestaurantInfoRow(
            icon = Icons.Filled.Schedule,
            title = "Opening Hours",
            value = "9:00 AM - 11:00 PM" // Replace with actual hours
        )
    }
}

@Composable
private fun RestaurantInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ScrollableCategories(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                enabled = true,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == category,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            )

        }
    }
}
@Composable
private fun CategoryHeader(category: String, itemCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$itemCount items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MealCard(meal: Meal, onItemClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = meal.image,
                    contentDescription = meal.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = meal.name ?: "Unnamed Meal", // Provide fallback for null
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meal.description ?: "No description available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${meal.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add to cart",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun OrderSummarySheet(
    restaurant: Restaurant,
    meals: List<Meal>,
    onCheckout: () -> Unit = {}
) {
    var cartItems by remember { mutableStateOf(emptyList<Meal>()) }

    // Initialize cart with some sample items from the meals list
    LaunchedEffect(meals) {
        if (meals.isNotEmpty() && cartItems.isEmpty()) {
            cartItems = listOf(meals.first()) // Default to first meal or implement proper cart logic
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Order",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Badge(
                modifier = Modifier.size(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Text(text = "${cartItems.size}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Empty cart",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your cart is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (meals.isNotEmpty()) {
                            cartItems = listOf(meals.random()) // Add random meal for demo
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Browse Menu")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${"%.2f".format(cartItems.sumOf { it.price })}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Delivery Fee",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$2.99", // Example fixed delivery fee
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${"%.2f".format(cartItems.sumOf { it.price } + 2.99)}", // Add delivery fee
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = cartItems.isNotEmpty(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Proceed to Checkout")
        }
    }
}

@Composable
private fun CartItemRow(meal: Meal) {
    var quantity by remember { mutableIntStateOf(1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = meal.image,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meal.name?.takeIf { it.isNotBlank() } ?: "No name available",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$${meal.price}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(
                onClick = { if (quantity > 1) quantity-- },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease")
            }

            Text(
                text = "$quantity",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = { quantity++ },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase")
            }
        }
    }
}
fun groupMealsByCategory(meals: List<Meal>): Map<String, List<Meal>> {
    return meals.groupBy { it.categoryName ?: "Other" }
}