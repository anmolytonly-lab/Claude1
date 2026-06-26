package com.shingar.salon.presentation.screens.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.navigation.NavRoutes
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.formatCurrency

@Composable
fun CustomersScreen(navController: NavController) {
    val customers by SalonRepository.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, email ->
                SalonRepository.addCustomer(
                    com.shingar.salon.data.model.Customer(name = name, phone = phone, email = email)
                )
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            SalonTopBar(title = "Customers", onBackClick = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PinkPrimary,
                contentColor = White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ScreenBackground)
        ) {
            SalonSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search customers...",
                modifier = Modifier.padding(16.dp)
            )

            if (filteredCustomers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = "No Customers Found",
                    subtitle = "Add your first customer to get started",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        Card(
                            onClick = { navController.navigate(NavRoutes.customerDetail(customer.id)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(SoftPurple.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        customer.name.first().toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = SoftPurple,
                                        fontSize = 22.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        customer.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(customer.phone, fontSize = 13.sp, color = TextSecondary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${customer.totalVisits} visits",
                                            fontSize = 12.sp,
                                            color = PinkPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(" · ", color = TextSecondary, fontSize = 12.sp)
                                        Text(
                                            customer.totalSpending.formatCurrency(),
                                            fontSize = 12.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MediumGray
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
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, email) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
