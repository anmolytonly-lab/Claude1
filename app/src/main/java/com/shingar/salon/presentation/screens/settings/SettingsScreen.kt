package com.shingar.salon.presentation.screens.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shingar.salon.data.repository.SalonRepository
import com.shingar.salon.presentation.components.SalonTopBar
import com.shingar.salon.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    val settings by SalonRepository.settings.collectAsState()
    var salonName by remember { mutableStateOf(settings.salonName) }
    var phone by remember { mutableStateOf(settings.phone) }
    var address by remember { mutableStateOf(settings.address) }
    var businessHours by remember { mutableStateOf(settings.businessHours) }
    var ownerName by remember { mutableStateOf(settings.ownerName) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SalonTopBar(title = "Settings", onBackClick = { navController.popBackStack() }) }
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
                        Icon(Icons.Default.Spa, contentDescription = null,
                            tint = White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(salonName, color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Premium Beauty Salon", color = White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }

            // Salon Details
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Salon Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                        TextButton(onClick = {
                            if (isEditing) {
                                SalonRepository.updateSettings(
                                    settings.copy(
                                        salonName = salonName, phone = phone,
                                        address = address, businessHours = businessHours,
                                        ownerName = ownerName
                                    )
                                )
                            }
                            isEditing = !isEditing
                        }) {
                            Text(if (isEditing) "Save" else "Edit", color = PinkPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = salonName, onValueChange = { salonName = it },
                            label = { Text("Salon Name") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ownerName, onValueChange = { ownerName = it },
                            label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone, onValueChange = { phone = it },
                            label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = address, onValueChange = { address = it },
                            label = { Text("Address") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = businessHours, onValueChange = { businessHours = it },
                            label = { Text("Business Hours") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary)
                        )
                    } else {
                        SettingRow(Icons.Default.Store, "Salon Name", salonName)
                        SettingRow(Icons.Default.Person, "Owner", ownerName)
                        SettingRow(Icons.Default.Phone, "Phone", phone)
                        SettingRow(Icons.Default.LocationOn, "Address", address)
                        SettingRow(Icons.Default.Schedule, "Business Hours", businessHours)
                    }
                }
            }

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingRow(Icons.Default.Info, "Version", "1.0.0")
                    SettingRow(Icons.Default.Code, "Build", "Demo Mode")
                    SettingRow(Icons.Default.Cloud, "Backend", "Local (Firebase Ready)")
                }
            }

            // Actions
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))

                    ActionButton(Icons.Default.Backup, "Backup Data", "Backup to Firebase") {}
                    ActionButton(Icons.Default.Restore, "Restore Data", "Restore from backup") {}
                    ActionButton(Icons.Default.Share, "Share App", "Share with other salon owners") {}
                    ActionButton(Icons.Default.Star, "Rate Us", "Rate on Play Store") {}
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Shingar Sallon Manager v1.0.0",
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenBackground)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray, modifier = Modifier.size(20.dp))
        }
    }
}
