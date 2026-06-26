package com.shingar.salon.presentation.screens.appointments

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
import androidx.compose.ui.platform.LocalContext
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
import com.shingar.salon.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(navController: NavController) {
    val appointments by SalonRepository.appointments.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<AppointmentStatus?>(null) }
    val context = LocalContext.current

    val filteredAppointments = remember(appointments, searchQuery, selectedFilter) {
        appointments.filter { appt ->
            val matchesSearch = searchQuery.isBlank() ||
                appt.customerName.contains(searchQuery, ignoreCase = true) ||
                appt.serviceName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = selectedFilter == null || appt.status == selectedFilter
            matchesSearch && matchesFilter
        }.sortedBy { it.dateTime }
    }

    Scaffold(
        topBar = {
            SalonTopBar(title = "Appointments", onBackClick = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavRoutes.ADD_APPOINTMENT) },
                containerColor = PinkPrimary,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
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
                placeholder = "Search appointments...",
                modifier = Modifier.padding(16.dp)
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PinkPrimary,
                        selectedLabelColor = White
                    )
                )
                AppointmentStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = if (selectedFilter == status) null else status },
                        label = { Text(status.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (status) {
                                AppointmentStatus.PENDING -> StatusPending
                                AppointmentStatus.CONFIRMED -> StatusConfirmed
                                AppointmentStatus.COMPLETED -> StatusCompleted
                                AppointmentStatus.CANCELLED -> StatusCancelled
                            },
                            selectedLabelColor = White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAppointments.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "No Appointments Found",
                    subtitle = "Add a new appointment or adjust your filters",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(filteredAppointments, key = { it.id }) { appointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(PinkLight.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            appointment.customerName.first().toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = PinkPrimary,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            appointment.customerName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            appointment.customerPhone,
                                            fontSize = 13.sp,
                                            color = TextSecondary
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

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = LightGray
                                )

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ContentCut, contentDescription = null,
                                                tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(appointment.serviceName, fontSize = 13.sp, color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null,
                                                tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(appointment.dateTime.formatDateTime(), fontSize = 13.sp, color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, contentDescription = null,
                                                tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Staff: ${appointment.staffName}", fontSize = 13.sp, color = TextSecondary)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            appointment.amount.formatCurrency(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = PinkPrimary
                                        )
                                        StatusChip(
                                            text = appointment.paymentStatus.label,
                                            color = when (appointment.paymentStatus) {
                                                com.shingar.salon.data.model.PaymentStatus.PAID -> PaymentPaid
                                                com.shingar.salon.data.model.PaymentStatus.UNPAID -> PaymentUnpaid
                                                com.shingar.salon.data.model.PaymentStatus.PARTIAL -> PaymentPartial
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        WhatsAppUtils.sendReminder(
                                            context, appointment.customerPhone,
                                            appointment.customerName, appointment.serviceName,
                                            appointment.dateTime.formatDateTime()
                                        )
                                    }) {
                                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp",
                                            tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {
                                        navController.navigate(NavRoutes.editAppointment(appointment.id))
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit",
                                            tint = InfoBlue, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {
                                        SalonRepository.deleteAppointment(appointment.id)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                                            tint = ErrorRed, modifier = Modifier.size(20.dp))
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
