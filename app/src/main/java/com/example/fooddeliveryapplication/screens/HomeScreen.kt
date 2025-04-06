package com.example.fooddeliveryapplication.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fooddeliveryapplication.data.model.Restaurant
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Categories for filtering
    val categories = listOf("All", "Italian", "Asian", "Mexican", "American", "Vegetarian", "Fast Food")

    // Fetch restaurants
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.getRestaurants()
                restaurants = response
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error fetching restaurants: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            topBar = {
                Column {
                    // Custom Top App Bar with user profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Hello, User!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                "Find your favorite food",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Profile picture
                        Image(
                            painter = rememberAsyncImagePainter(model = "https://randomuser.me/api/portraits/women/44.jpg"),
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar
                    SearchBar(searchQuery) { query -> searchQuery = query }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories Horizontal Scroll
                    CategoriesSection(categories, selectedCategory) { category ->
                        selectedCategory = category
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("favorites") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "Favorites",
                        tint = Color.White
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val filteredRestaurants = restaurants.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            (it.location?.contains(
                                searchQuery,
                                ignoreCase = true
                            ) == true) || it.cuisineType?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true
                }.filter {
                    selectedCategory == "All" || it.cuisineType?.contains(selectedCategory, ignoreCase = true) == true
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Promo Banner Section
                    item {
                        PromoBanner()
                    }

                    // Popular Restaurants Section
                    item {
                        SectionHeader(
                            title = "Popular Restaurants",
                            actionText = "See All",
                            onActionClick = { /* Handle see all */ }
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(restaurants.take(5)) { restaurant ->
                                PopularRestaurantCard(
                                    restaurant = restaurant,
                                    onClick = { navController.navigate("restaurantDetails/${restaurant.id}") },
                                    isFavorite = favorites.contains(restaurant.id.toString()),
                                    onFavoriteClick = { id ->
                                        favorites = if (favorites.contains(id)) {
                                            favorites - id
                                        } else {
                                            favorites + id
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // All Restaurants Section
                    item {
                        SectionHeader(
                            title = "All Restaurants",
                            actionText = "See All",
                            onActionClick = { /* Handle see all */ }
                        )
                    }

                    items(filteredRestaurants) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { navController.navigate("restaurantDetails/${restaurant.id}") },
                            isFavorite = favorites.contains(restaurant.id.toString()),
                            onFavoriteClick = { id ->
                                favorites = if (favorites.contains(id)) {
                                    favorites - id
                                } else {
                                    favorites + id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    "Special Offer!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Get 20% off on your first order",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Handle promo click */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Order Now")
                }
            }

            Image(
                painter = rememberAsyncImagePainter(model = "https://i.imgur.com/5XJz7qL.png"),
                contentDescription = "Promo Food",
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = category == selectedCategory,
                onSelected = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(category: String, isSelected: Boolean, onSelected: () -> Unit) {
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200)
    )

    val contentColor by animateColorAsState(
        if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200)
    )

    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(category) },
        modifier = Modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) // Uses default colors and borders
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search for restaurants or cuisines...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Restaurant Image with Favorite Button and Rating
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(model = restaurant.image),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Rating Chip
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                restaurant.rating?.toString() ?: "4.5",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Favorite Button
                IconButton(
                    onClick = { onFavoriteClick(restaurant.id.toString()) },
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Restaurant Info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (restaurant.deliveryTime != null) {
                        Text(
                            text = "${restaurant.deliveryTime} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (!restaurant.location.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = restaurant.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!restaurant.cuisineType.isNullOrEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        restaurant.cuisineType.split(",").forEach { cuisine ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = cuisine.trim(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PopularRestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Restaurant Image with Favorite Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(model = restaurant.image),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Favorite Button
                IconButton(
                    onClick = { onFavoriteClick(restaurant.id.toString()) },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Restaurant Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = restaurant.rating?.toString() ?: "4.5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        Icons.Rounded.DeliveryDining,
                        contentDescription = "Delivery",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = restaurant.deliveryFee?.let { if (it == 0) "Free" else "$$it" } ?: "Free",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}