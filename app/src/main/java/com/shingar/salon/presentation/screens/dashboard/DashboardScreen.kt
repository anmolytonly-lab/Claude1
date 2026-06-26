package com.shingar.salon.presentation.screens.dashboard

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.AppointmentStatus
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.navigation.NavRoutes
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.formatCurrency
import com.shingar.salon.utils.formatTime
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val appointments by SalonRepository.appointments.collectAsState()
    val stats = remember(appointments) { SalonRepository.getDashboardStats() }
    val todayAppointments = remember(appointments) {
        appointments.filter { it.dateTime.toLocalDate() == LocalDate.now() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shingar Sallon", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Welcome back, Owner", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.AI_ASSISTANT) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
                    }
                    IconButton(onClick = { navController.navigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue,
                    titleContentColor = White,
                    actionIconContentColor = White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ScreenBackground),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Revenue Card
            item {
                GradientCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = listOf(DarkBlue, SoftPurple)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Today's Earnings", color = White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                stats.todayEarnings.formatCurrency(),
                                color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Monthly: ${stats.monthlyRevenue.formatCurrency()}",
                                color = White.copy(alpha = 0.7f), fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null,
                                tint = White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Stats Grid
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = "Appointments",
                        value = "${stats.todayAppointments}",
                        icon = Icons.Default.CalendarMonth,
                        iconTint = PinkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Customers",
                        value = "${stats.totalCustomers}",
                        icon = Icons.Default.People,
                        iconTint = SoftPurple,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Pending",
                        value = "${stats.pendingPayments}",
                        icon = Icons.Default.PendingActions,
                        iconTint = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions
            item {
                SectionHeader(title = "Quick Actions")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton("Book", Icons.Default.AddCircle, PinkPrimary) {
                        navController.navigate(NavRoutes.ADD_APPOINTMENT)
                    }
                    QuickActionButton("Customers", Icons.Default.People, SoftPurple) {
                        navController.navigate(NavRoutes.CUSTOMERS)
                    }
                    QuickActionButton("Services", Icons.Default.ContentCut, DarkBlue) {
                        navController.navigate(NavRoutes.SERVICES)
                    }
                    QuickActionButton("Offers", Icons.Default.LocalOffer, WarningOrange) {
                        navController.navigate(NavRoutes.OFFERS)
                    }
                    QuickActionButton("Reports", Icons.Default.BarChart, SuccessGreen) {
                        navController.navigate(NavRoutes.REPORTS)
                    }
                }
            }

            // Today's Appointments
            item {
                SectionHeader(
                    title = "Today's Appointments",
                    actionText = "View All",
                    onActionClick = { navController.navigate(NavRoutes.APPOINTMENTS) }
                )
            }

            if (todayAppointments.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No Appointments Today",
                        subtitle = "Start by booking a new appointment"
                    )
                }
            } else {
                items(todayAppointments.take(5)) { appointment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PinkLight.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    appointment.customerName.first().toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = PinkPrimary,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    appointment.customerName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${appointment.serviceName} - ${appointment.dateTime.formatTime()}",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            StatusChip(
                                text = appointment.status.label,
                                color = when (appointment.status) {
                                    AppointmentStatus.PENDING -> StatusPending
                                    AppointmentStatus.CONFIRMED -> StatusConfirmed
                                    AppointmentStatus.COMPLETED -> StatusCompleted
                                    AppointmentStatus.CANCELLED -> StatusCancelled
                                }
                            )
                        }
                    }
                }
            }

            // Navigation Menu
            item {
                SectionHeader(title = "Manage")
                Spacer(modifier = Modifier.height(4.dp))
            }

            val menuItems = listOf(
                Triple("Appointments", Icons.Default.CalendarMonth, NavRoutes.APPOINTMENTS),
                Triple("Customers", Icons.Default.People, NavRoutes.CUSTOMERS),
                Triple("Services", Icons.Default.ContentCut, NavRoutes.SERVICES),
                Triple("Staff", Icons.Default.Groups, NavRoutes.STAFF),
                Triple("Offers", Icons.Default.LocalOffer, NavRoutes.OFFERS),
                Triple("Payments", Icons.Default.Payment, NavRoutes.PAYMENTS),
                Triple("Reports", Icons.Default.BarChart, NavRoutes.REPORTS),
                Triple("AI Assistant", Icons.Default.AutoAwesome, NavRoutes.AI_ASSISTANT),
            )

            items(menuItems) { (title, icon, route) ->
                Card(
                    onClick = { navController.navigate(route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null,
                            tint = MediumGray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
