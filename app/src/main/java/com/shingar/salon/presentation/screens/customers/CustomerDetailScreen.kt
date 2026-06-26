package com.shingar.salon.presentation.screens.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.SalonTopBar
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.*

@Composable
fun CustomerDetailScreen(navController: NavController, customerId: String) {
    val customers by SalonRepository.customers.collectAsState()
    val customer = remember(customerId, customers) { customers.find { it.id == customerId } }
    val context = LocalContext.current

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Customer not found", color = TextSecondary)
        }
        return
    }

    Scaffold(
        topBar = {
            SalonTopBar(title = "Customer Profile", onBackClick = { navController.popBackStack() })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ScreenBackground)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DarkBlue, SoftPurple)))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            customer.name.first().toString(),
                            fontWeight = FontWeight.Bold,
                            color = White,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(customer.name, color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(customer.phone, color = White.copy(alpha = 0.8f), fontSize = 14.sp)
                    if (customer.email.isNotBlank()) {
                        Text(customer.email, color = White.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                }
            }

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem("Visits", "${customer.totalVisits}", Icons.Default.CalendarMonth, PinkPrimary, Modifier.weight(1f))
                StatItem("Spent", customer.totalSpending.formatCurrency(), Icons.Default.CurrencyRupee, SuccessGreen, Modifier.weight(1f))
            }

            // Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)

                    DetailRow(Icons.Default.DateRange, "Member Since", customer.joinDate.formatDate())
                    customer.lastVisit?.let {
                        DetailRow(Icons.Default.Schedule, "Last Visit", it.formatDate())
                    }
                    customer.birthday?.let {
                        DetailRow(Icons.Default.Cake, "Birthday", it.formatDate())
                    }
                    if (customer.notes.isNotBlank()) {
                        DetailRow(Icons.Default.Notes, "Notes", customer.notes)
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        WhatsAppUtils.sendMessage(context, customer.phone, "Hello ${customer.name}! Thank you for being a valued customer at Shingar Sallon!")
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Text(label, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
