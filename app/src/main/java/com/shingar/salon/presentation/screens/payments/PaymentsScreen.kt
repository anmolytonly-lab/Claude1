package com.shingar.salon.presentation.screens.payments

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.PaymentMethod
import com.shingar.salon.data.model.PaymentStatus
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.formatCurrency
import com.shingar.salon.utils.formatDate
import java.time.LocalDate

@Composable
fun PaymentsScreen(navController: NavController) {
    val payments by SalonRepository.payments.collectAsState()
    var selectedFilter by remember { mutableStateOf<PaymentStatus?>(null) }

    val filteredPayments = remember(payments, selectedFilter) {
        payments.filter { selectedFilter == null || it.status == selectedFilter }
            .sortedByDescending { it.date }
    }

    val today = LocalDate.now()
    val todayTotal = remember(payments) {
        payments.filter { it.date == today && it.status == PaymentStatus.PAID }.sumOf { it.amount }
    }
    val monthlyTotal = remember(payments) {
        payments.filter { it.date.month == today.month && it.date.year == today.year && it.status == PaymentStatus.PAID }
            .sumOf { it.amount }
    }
    val pendingTotal = remember(payments) {
        payments.filter { it.status == PaymentStatus.UNPAID }.sumOf { it.amount }
    }

    Scaffold(
        topBar = { SalonTopBar(title = "Payments", onBackClick = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(ScreenBackground)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Today", fontSize = 12.sp, color = TextSecondary)
                        Text(todayTotal.formatCurrency(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SuccessGreen)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monthly", fontSize = 12.sp, color = TextSecondary)
                        Text(monthlyTotal.formatCurrency(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = InfoBlue)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pending", fontSize = 12.sp, color = TextSecondary)
                        Text(pendingTotal.formatCurrency(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ErrorRed)
                    }
                }
            }

            // Filter
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = selectedFilter == null, onClick = { selectedFilter = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PinkPrimary, selectedLabelColor = White))
                FilterChip(selected = selectedFilter == PaymentStatus.PAID, onClick = {
                    selectedFilter = if (selectedFilter == PaymentStatus.PAID) null else PaymentStatus.PAID
                }, label = { Text("Paid") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SuccessGreen, selectedLabelColor = White))
                FilterChip(selected = selectedFilter == PaymentStatus.UNPAID, onClick = {
                    selectedFilter = if (selectedFilter == PaymentStatus.UNPAID) null else PaymentStatus.UNPAID
                }, label = { Text("Unpaid") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ErrorRed, selectedLabelColor = White))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(filteredPayments, key = { it.id }) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape)
                                    .background(
                                        if (payment.status == PaymentStatus.PAID) SuccessGreen.copy(alpha = 0.1f)
                                        else ErrorRed.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (payment.status == PaymentStatus.PAID) Icons.Default.CheckCircle
                                    else Icons.Default.Pending,
                                    contentDescription = null,
                                    tint = if (payment.status == PaymentStatus.PAID) SuccessGreen else ErrorRed
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(payment.customerName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(payment.serviceName, fontSize = 13.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(payment.date.formatDate(), fontSize = 12.sp, color = TextSecondary)
                                    if (payment.status == PaymentStatus.PAID) {
                                        Text(" · ${payment.method.label}", fontSize = 12.sp, color = InfoBlue)
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(payment.amount.formatCurrency(), fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp, color = if (payment.status == PaymentStatus.PAID) SuccessGreen else ErrorRed)
                                if (payment.status == PaymentStatus.UNPAID) {
                                    TextButton(
                                        onClick = {
                                            SalonRepository.updatePaymentStatus(payment.id, PaymentStatus.PAID, PaymentMethod.CASH)
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Mark Paid", fontSize = 12.sp, color = PinkPrimary)
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
