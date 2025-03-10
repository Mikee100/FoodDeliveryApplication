package com.example.fooddeliveryapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fooddeliveryapplication.data.model.Order
import com.example.fooddeliveryapplication.data.model.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun OrderStatusScreen(navController: NavController, orderId: String?) {

    var order by remember { mutableStateOf<Order?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch order details
    LaunchedEffect(orderId) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.getOrderDetails(orderId ?: "")
                if (response.isSuccessful) {
                    order = response.body()
                } else {
                    error = "Error fetching order details: ${response.code()}"
                }
            } catch (e: Exception) {
                error = "Error fetching order details: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
    } else if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Order details not available.", color = MaterialTheme.colorScheme.error)
        }
    } else {
        OrderStatusUI(order = order!!)
    }
}
@Composable
fun OrderStatusUI(order: Order) {
    val statusSteps = listOf(
        "Order Placed" to "🛒",
        "In the Kitchen" to "👨‍🍳",
        "Out for Delivery" to "🚚",
        "Delivered" to "🏠"
    )

    val currentStepIndex = statusSteps.indexOfFirst { it.first == order.status }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Order Status",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display status steps
        statusSteps.forEachIndexed { index, (label, icon) ->
            StatusStep(
                label = label,
                icon = icon,
                isActive = index <= currentStepIndex,
                isLast = index == statusSteps.size - 1
            )
        }

        // Current status
        Text(
            text = "Current Status: ${order.status}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun StatusStep(label: String, icon: String, isActive: Boolean, isLast: Boolean) {
    val activeColor = Color.Green
    val inactiveColor = Color.Gray

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon and label
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displayMedium,
                color = if (isActive) activeColor else inactiveColor
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isActive) activeColor else inactiveColor
        )

        // Progress line (if not the last step)
        if (!isLast) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(2.dp),
                color = if (isActive) activeColor else inactiveColor
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}