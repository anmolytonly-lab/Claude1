package com.shingar.salon.presentation.screens.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.SalonService
import com.shingar.salon.data.model.ServiceCategory
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(navController: NavController) {
    val services by SalonRepository.services.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredServices = remember(services, searchQuery, selectedCategory) {
        services.filter { svc ->
            val matchesSearch = searchQuery.isBlank() || svc.name.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == null || svc.category == selectedCategory
            matchesSearch && matchesCat
        }
    }

    if (showAddDialog) {
        AddServiceDialog(
            onDismiss = { showAddDialog = false },
            onSave = { service -> SalonRepository.addService(service); showAddDialog = false }
        )
    }

    Scaffold(
        topBar = { SalonTopBar(title = "Services", onBackClick = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PinkPrimary, contentColor = White
            ) { Icon(Icons.Default.Add, contentDescription = "Add Service") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(ScreenBackground)
        ) {
            SalonSearchBar(
                query = searchQuery, onQueryChange = { searchQuery = it },
                placeholder = "Search services...",
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PinkPrimary, selectedLabelColor = White
                        )
                    )
                }
                items(ServiceCategory.entries.toList()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftPurple, selectedLabelColor = White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredServices.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ContentCut,
                    title = "No Services Found",
                    subtitle = "Add services or adjust your filters",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(filteredServices, key = { it.id }) { service ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape)
                                        .background(PinkPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when (service.category) {
                                            ServiceCategory.MAKEUP -> Icons.Default.Face
                                            ServiceCategory.HAIR -> Icons.Default.ContentCut
                                            ServiceCategory.SKIN -> Icons.Default.Spa
                                            ServiceCategory.NAILS -> Icons.Default.Brush
                                            ServiceCategory.BODY -> Icons.Default.FitnessCenter
                                            ServiceCategory.GENERAL -> Icons.Default.Star
                                        },
                                        contentDescription = null, tint = PinkPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(service.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text(service.description, fontSize = 13.sp, color = TextSecondary, maxLines = 1)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusChip(text = service.category.label, color = SoftPurple)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Schedule, contentDescription = null,
                                            modifier = Modifier.size(14.dp), tint = TextSecondary)
                                        Text(" ${service.duration} min", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                                Text(
                                    service.price.formatCurrency(),
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PinkPrimary
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
private fun AddServiceDialog(onDismiss: () -> Unit, onSave: (SalonService) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Service", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = duration, onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(SalonService(
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        duration = duration.toIntOrNull() ?: 30,
                        description = description
                    ))
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
