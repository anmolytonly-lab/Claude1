package com.shingar.salon.presentation.screens.staff

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
import com.shingar.salon.data.model.Staff
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.*
import com.shingar.salon.ui.theme.*
import com.shingar.salon.utils.formatDate

@Composable
fun StaffScreen(navController: NavController) {
    val staffList by SalonRepository.staff.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, role ->
                SalonRepository.addStaff(Staff(name = name, phone = phone, role = role))
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = { SalonTopBar(title = "Staff", onBackClick = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PinkPrimary, contentColor = White
            ) { Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(ScreenBackground)
        ) {
            if (staffList.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Groups,
                    title = "No Staff Members",
                    subtitle = "Add your first staff member",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(staffList, key = { it.id }) { staff ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(52.dp).clip(CircleShape)
                                            .background(DarkBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            staff.name.first().toString(),
                                            fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 22.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(staff.name, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                                        Text(staff.role, fontSize = 14.sp, color = TextSecondary)
                                        Text(staff.phone, fontSize = 13.sp, color = TextSecondary)
                                    }
                                    if (staff.isActive) {
                                        StatusChip(text = "Active", color = SuccessGreen)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = LightGray)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${staff.appointmentsCompleted}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PinkPrimary)
                                        Text("Completed", fontSize = 12.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null,
                                                tint = WarningOrange, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${staff.rating}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Text("Rating", fontSize = 12.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(staff.joinDate.formatDate(), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text("Joined", fontSize = 12.sp, color = TextSecondary)
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
private fun AddStaffDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = role, onValueChange = { role = it },
                    label = { Text("Role") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone, role) }, enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
