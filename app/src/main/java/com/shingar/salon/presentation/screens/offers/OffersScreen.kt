package com.shingar.salon.presentation.screens.offers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.Offer
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.*

@Composable
fun OffersScreen(navController: NavController) {
    val offers by SalonRepository.offers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showAddDialog) {
        AddOfferDialog(
            onDismiss = { showAddDialog = false },
            onSave = { offer -> SalonRepository.addOffer(offer); showAddDialog = false }
        )
    }

    Scaffold(
        topBar = { SalonTopBar(title = "Offers", onBackClick = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PinkPrimary, contentColor = White
            ) { Icon(Icons.Default.Add, contentDescription = "Add Offer") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(ScreenBackground)
        ) {
            if (offers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.LocalOffer,
                    title = "No Active Offers",
                    subtitle = "Create your first offer to attract customers",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(offers, key = { it.id }) { offer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(PinkPrimary.copy(alpha = 0.05f), SoftPurple.copy(alpha = 0.05f))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(offer.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(offer.description, fontSize = 14.sp, color = TextSecondary)
                                    }
                                    if (offer.discount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = PinkPrimary
                                        ) {
                                            Text(
                                                "${offer.discount}% OFF",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = LightGray)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null,
                                                tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "${offer.startDate.formatDate()} - ${offer.endDate.formatDate()}",
                                                fontSize = 12.sp, color = TextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        StatusChip(
                                            text = if (offer.isActive) "Active" else "Inactive",
                                            color = if (offer.isActive) SuccessGreen else ErrorRed
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            WhatsAppUtils.shareOffer(context, "", offer.title, offer.description)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddOfferDialog(onDismiss: () -> Unit, onSave: (Offer) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Offer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Offer Title") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), minLines = 2, maxLines = 3)
                OutlinedTextField(value = discount, onValueChange = { discount = it },
                    label = { Text("Discount %") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Offer(title = title, description = description, discount = discount.toIntOrNull() ?: 0))
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
