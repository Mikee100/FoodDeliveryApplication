@file:Suppress("DEPRECATION")

package com.example.fooddeliveryapplication.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.fooddeliveryapplication.data.model.Meal
import com.example.fooddeliveryapplication.data.model.MpesaPaymentRequest
import com.example.fooddeliveryapplication.data.model.OrderRequest
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailsScreen(navController: NavController, mealId: String?) {
    Log.d("MealDebug", "Screen launched with mealId: $mealId")

    var meal by remember { mutableStateOf<Meal?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch meal details
    LaunchedEffect(mealId) {
        Log.d("MealDebug", "Starting data fetch")

        if (mealId.isNullOrEmpty()) {
            error = "Invalid meal ID"
            loading = false
            Log.e("MealDebug", "Invalid meal ID provided")
            return@LaunchedEffect
        }

        coroutineScope.launch {
            try {
                Log.d("MealDebug", "Making API call for meal $mealId")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMealDetails(mealId ?: "")
                }

                // Extract meal from response
                response.data.let {
                    meal = Meal(
                        id = it.id,
                        name = it.name ?: "Unnamed Meal",
                        description = it.description ?: "",
                        image = it.image ?: "",
                        categoryName = it.categoryName ?: "",
                        price = it.price,
                        restaurantName = it.restaurantName ?: "Unknown Restaurant"
                    )
                }

                Log.d("MealDebug", "Meal received: $meal")

            } catch (e: Exception) {
                error = "Error: ${e.localizedMessage}"
                Log.e("MealDebug", "API call failed", e)
            } finally {
                loading = false
                Log.d("MealDebug", "Loading completed.")
            }
        }



    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meal Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                loading -> FullScreenLoading()
                error != null -> ErrorScreen(error!!) {
                    Log.d("MealDebug", "Retrying fetch...")
                    loading = true
                    error = null

                }
                meal == null -> ErrorScreen("Meal not found") { navController.popBackStack() }
                else -> SafeMealInfo(meal = meal!!, navController = navController, snackbarHostState)
            }
        }
    }
}
@Composable
private fun SafeMealInfo(meal: Meal, navController: NavController, snackbarHostState: SnackbarHostState) {
    val safeMeal = remember(meal) {
        meal.copy(
            name = meal.name ?: "Unnamed Meal",
            description = meal.description ?: "No description available",
            image = meal.image ?: ""
        )
    }
    MealInfo(meal = safeMeal, navController = navController, snackbarHostState)
}

@Composable
fun MealInfo(meal: Meal, navController: NavController, snackbarHostState: SnackbarHostState) {
    var quantity by remember { mutableIntStateOf(1) }
    var specifics by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Log.d("MealDebug", "MealInfo received: ${meal.toString()}")

    // Log individual fields for clarity
    Log.d("MealDebug", """
        Meal Details:
        ID: ${meal.id}
        Name: ${meal.name}
        Description: ${meal.description}
        Price: ${meal.price}
        Image: ${meal.image}
        Category: ${meal.categoryName}
        Restaurant: ${meal.restaurantName}
    """.trimIndent())


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Safe Image loading
        SubcomposeAsyncImage(
            model = meal.image.takeIf { !it.isNullOrEmpty() },
            contentDescription = meal.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Fastfood,
                        contentDescription = "Meal image placeholder",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Meal Name and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.name ?: "Unnamed Meal",  // Fallback for null
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ksh ${"%.2f".format(meal.price)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meal Description
            Text(
                text = meal.description ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )


            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Quantity Selector
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                }
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                }
            }

            // Special Instructions
            Text(
                text = "Special Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = specifics,
                onValueChange = { specifics = it },
                label = { Text("Add specifics (e.g., no onions)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Payment Section
            Text(
                text = "Payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.length <= 12 && it.all { c -> c.isDigit() }) {
                        phoneNumber = it
                    }
                },
                label = { Text("M-Pesa Phone Number (e.g., 2547XXXXXXXX)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("+254") }
            )

            // Order Button
            Button(
                onClick = {
                    if (phoneNumber.length < 9) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please enter a valid phone number")
                        }
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.instance.processMpesaPayment(
                                    MpesaPaymentRequest(
                                        phoneNumber = "254${phoneNumber.takeLast(9)}",
                                        amount = meal.price * quantity
                                    )
                                )
                            }

                            if (response.isSuccessful) {
                                response.body()?.let { paymentResponse ->
                                    // Create order request
                                    val orderRequest = OrderRequest(
                                        mealId = meal.id.toString(),
                                        specifics = specifics,
                                        phoneNumber = phoneNumber,
                                        price = meal.price * quantity
                                    )

                                    // Navigate to order status
                                    navController.navigate("orderStatus/${paymentResponse.checkoutRequestId}")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Payment failed: ${response.message()}")
                            }
                        } catch (e: Exception) {
                            Log.e("OrderError", "Failed to place order", e)
                            snackbarHostState.showSnackbar("Order failed: ${e.message ?: "Unknown error"}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Place Order - Ksh ${"%.2f".format(meal.price * quantity)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading meal details...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}