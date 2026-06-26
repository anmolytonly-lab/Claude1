package com.shingar.salon.presentation.screens.reports

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.PaymentStatus
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.formatCurrency
import java.time.LocalDate

@Composable
fun ReportsScreen(navController: NavController) {
    val payments by SalonRepository.payments.collectAsState()
    val appointments by SalonRepository.appointments.collectAsState()
    val customers by SalonRepository.customers.collectAsState()
    val services by SalonRepository.services.collectAsState()

    val today = LocalDate.now()

    val todayRevenue = remember(payments) {
        payments.filter { it.date == today && it.status == PaymentStatus.PAID }.sumOf { it.amount }
    }
    val monthlyRevenue = remember(payments) {
        payments.filter { it.date.month == today.month && it.date.year == today.year && it.status == PaymentStatus.PAID }
            .sumOf { it.amount }
    }
    val todayAppointments = remember(appointments) {
        appointments.count { it.dateTime.toLocalDate() == today }
    }
    val monthlyAppointments = remember(appointments) {
        appointments.count { it.dateTime.toLocalDate().month == today.month }
    }

    // Popular services by count
    val popularServices = remember(appointments) {
        appointments.groupBy { it.serviceName }
            .mapValues { it.value.size }
            .entries.sortedByDescending { it.value }
            .take(5)
    }

    // Revenue by payment method
    val revenueByMethod = remember(payments) {
        payments.filter { it.status == PaymentStatus.PAID }
            .groupBy { it.method.label }
            .mapValues { it.value.sumOf { p -> p.amount } }
    }

    Scaffold(
        topBar = { SalonTopBar(title = "Reports", onBackClick = { navController.popBackStack() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(ScreenBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Revenue Overview
            item {
                GradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = listOf(DarkBlue, SoftPurple)
                ) {
                    Text("Revenue Overview", color = White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Today", color = White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(todayRevenue.formatCurrency(), color = White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("This Month", color = White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(monthlyRevenue.formatCurrency(), color = White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatsCard("Today's Appts", "$todayAppointments", Icons.Default.CalendarToday,
                        PinkPrimary, Modifier.weight(1f))
                    StatsCard("Monthly Appts", "$monthlyAppointments", Icons.Default.CalendarMonth,
                        SoftPurple, Modifier.weight(1f))
                    StatsCard("Total Clients", "${customers.size}", Icons.Default.People,
                        DarkBlue, Modifier.weight(1f))
                }
            }

            // Popular Services
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Popular Services", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (popularServices.isEmpty()) {
                            Text("No data yet", color = TextSecondary, fontSize = 14.sp)
                        } else {
                            val maxCount = popularServices.maxOf { it.value }.toFloat()
                            popularServices.forEach { (name, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontSize = 14.sp, modifier = Modifier.width(120.dp))
                                    Box(modifier = Modifier.weight(1f).height(24.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(count / maxCount)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    Brush.horizontalGradient(listOf(PinkPrimary, SoftPurple))
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("$count", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PinkPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Revenue by Payment Method
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Revenue by Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        val methodColors = mapOf(
                            "Cash" to SuccessGreen, "UPI" to InfoBlue, "Card" to SoftPurple
                        )

                        revenueByMethod.forEach { (method, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(12.dp).clip(CircleShape)
                                            .background(methodColors[method] ?: MediumGray)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(method, fontSize = 14.sp)
                                }
                                Text(amount.formatCurrency(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Customer Growth
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Customer Insights", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        val avgSpending = if (customers.isNotEmpty())
                            customers.map { it.totalSpending }.average() else 0.0
                        val totalRevenue = customers.sumOf { it.totalSpending }
                        val avgVisits = if (customers.isNotEmpty())
                            customers.map { it.totalVisits.toDouble() }.average() else 0.0

                        InsightRow("Total Revenue (All Time)", totalRevenue.formatCurrency(), SuccessGreen)
                        InsightRow("Average Spending/Customer", avgSpending.formatCurrency(), InfoBlue)
                        InsightRow("Average Visits/Customer", String.format("%.1f", avgVisits), SoftPurple)
                        InsightRow("Total Services Available", "${services.size}", PinkPrimary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
    }
}
