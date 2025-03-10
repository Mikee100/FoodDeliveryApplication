package com.example.fooddeliveryapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fooddeliveryapplication.data.model.Meal
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MealDetailsScreen(navController: NavController, mealId: String?) {
    var meal by remember { mutableStateOf<Meal?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch meal details
    LaunchedEffect(mealId) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.getMealDetails(mealId ?: "")
                meal = response
            } catch (e: Exception) {
                error = "Error fetching meal details: ${e.message}"
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
    } else if (error != null) {
        // Show an error message
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
    } else if (meal == null) {
        // Show a message if no meal is found
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Meal details not available.", color = MaterialTheme.colorScheme.error)
        }
    } else {
        // Display meal details
        MealInfo(meal = meal!!, navController = navController)
    }
}

@Composable
fun MealInfo(meal: Meal, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Meal Image
        if (!meal.image.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(model = meal.image),
                contentDescription = meal.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Meal Name
        Text(
            text = meal.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Meal Description
        if (!meal.description.isNullOrEmpty()) {
            Text(
                text = meal.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Meal Price
        Text(
            text = "Price: Ksh ${meal.price}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Add Specifics (e.g., notes or customizations)
        var specifics by remember { mutableStateOf("") }
        OutlinedTextField(
            value = specifics,
            onValueChange = { specifics = it },
            label = { Text("Add specifics (e.g., no onions)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Payment Options
        PaymentOptions(meal = meal, specifics = specifics, navController = navController)
    }
}

@Composable
fun PaymentOptions(meal: Meal, specifics: String, navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // M-Pesa Payment
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Enter M-Pesa phone number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                // Handle M-Pesa payment
                handleMpesaPayment(meal, phoneNumber, specifics)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Pay with M-Pesa")
        }

        // Stripe Payment (if applicable)
        Button(
            onClick = {
                // Handle Stripe payment
                handleStripePayment(meal, specifics)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Pay with Stripe")
        }
    }
}

fun handleMpesaPayment(meal: Meal, phoneNumber: String, specifics: String) {
    // Call your backend API to initiate M-Pesa payment
    // Example:
    // RetrofitClient.instance.initiateMpesaPayment(phoneNumber, meal.price, specifics)
}

fun handleStripePayment(meal: Meal, specifics: String) {
    // Call your backend API to initiate Stripe payment
    // Example:
    // RetrofitClient.instance.initiateStripePayment(meal.price, specifics)
}