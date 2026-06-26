package com.shingar.salon.presentation.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.model.*
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.SalonTopBar
import com.shingar.salon.ui.theme.*
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    navController: NavController,
    appointmentId: String? = null
) {
    val appointments by SalonRepository.appointments.collectAsState()
    val services by SalonRepository.services.collectAsState()
    val staffList by SalonRepository.staff.collectAsState()

    val existingAppointment = remember(appointmentId, appointments) {
        appointmentId?.let { id -> appointments.find { it.id == id } }
    }
    val isEditing = existingAppointment != null

    var customerName by remember { mutableStateOf(existingAppointment?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(existingAppointment?.customerPhone ?: "") }
    var selectedService by remember { mutableStateOf(existingAppointment?.serviceName ?: "") }
    var selectedStaff by remember { mutableStateOf(existingAppointment?.staffName ?: "") }
    var selectedStatus by remember { mutableStateOf(existingAppointment?.status ?: AppointmentStatus.PENDING) }
    var notes by remember { mutableStateOf(existingAppointment?.notes ?: "") }
    var amount by remember { mutableStateOf(existingAppointment?.amount?.toString() ?: "") }

    var serviceExpanded by remember { mutableStateOf(false) }
    var staffExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SalonTopBar(
                title = if (isEditing) "Edit Appointment" else "New Appointment",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ScreenBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        singleLine = true
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Appointment Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)

                    // Service Dropdown
                    ExposedDropdownMenuBox(
                        expanded = serviceExpanded,
                        onExpandedChange = { serviceExpanded = !serviceExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedService,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Service") },
                            leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        ExposedDropdownMenu(expanded = serviceExpanded, onDismissRequest = { serviceExpanded = false }) {
                            services.forEach { service ->
                                DropdownMenuItem(
                                    text = { Text("${service.name} - ₹${service.price.toInt()}") },
                                    onClick = {
                                        selectedService = service.name
                                        amount = service.price.toString()
                                        serviceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Staff Dropdown
                    ExposedDropdownMenuBox(
                        expanded = staffExpanded,
                        onExpandedChange = { staffExpanded = !staffExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStaff,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign Staff") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = staffExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        ExposedDropdownMenu(expanded = staffExpanded, onDismissRequest = { staffExpanded = false }) {
                            staffList.forEach { staff ->
                                DropdownMenuItem(
                                    text = { Text("${staff.name} - ${staff.role}") },
                                    onClick = {
                                        selectedStaff = staff.name
                                        staffExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            AppointmentStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.label) },
                                    onClick = {
                                        selectedStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val serviceObj = services.find { it.name == selectedService }
                    val staffObj = staffList.find { it.name == selectedStaff }
                    val appointment = Appointment(
                        id = existingAppointment?.id ?: java.util.UUID.randomUUID().toString(),
                        customerName = customerName,
                        customerPhone = customerPhone,
                        serviceId = serviceObj?.id ?: "",
                        serviceName = selectedService,
                        staffId = staffObj?.id ?: "",
                        staffName = selectedStaff,
                        dateTime = existingAppointment?.dateTime ?: LocalDateTime.now(),
                        status = selectedStatus,
                        notes = notes,
                        amount = amount.toDoubleOrNull() ?: 0.0
                    )
                    if (isEditing) {
                        SalonRepository.updateAppointment(appointment)
                    } else {
                        SalonRepository.addAppointment(appointment)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                enabled = customerName.isNotBlank() && selectedService.isNotBlank()
            ) {
                Icon(if (isEditing) Icons.Default.Save else Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditing) "Update Appointment" else "Book Appointment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
