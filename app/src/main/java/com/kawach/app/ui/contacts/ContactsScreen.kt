package com.kawach.app.ui.contacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kawach.app.KawachApp
import com.kawach.app.data.local.entity.EmergencyContact
import com.kawach.app.util.SmsHelper

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as KawachApp

    val viewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModel.Factory(app.contactRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val smsPermission = rememberPermissionState(Manifest.permission.SEND_SMS)
    val callPermission = rememberPermissionState(Manifest.permission.CALL_PHONE)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // ── Personal Emergency Contacts ────────────────────────────────────
        item {
            SectionHeader(
                title = "Emergency Contacts",
                subtitle = "SOS alert इन्हें जाएगा",
                onAdd = { viewModel.showAddDialog(isNeighbourhood = false) }
            )
        }

        if (uiState.personalContacts.isEmpty()) {
            item {
                EmptyContactsHint("अभी कोई contact नहीं — जोड़ें!")
            }
        }

        items(uiState.personalContacts, key = { it.id }) { contact ->
            ContactCard(
                contact = contact,
                onCall = {
                    if (callPermission.status.isGranted) {
                        context.startActivity(
                            Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.phoneNumber}"))
                        )
                    } else callPermission.launchPermissionRequest()
                },
                onEdit = { viewModel.showEditDialog(contact) },
                onDelete = { viewModel.deleteContact(contact) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── Neighbourhood Group ────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Neighbourhood Group",
                subtitle = "Community alert इन्हें जाएगा",
                onAdd = { viewModel.showAddDialog(isNeighbourhood = true) }
            )
        }

        if (uiState.neighbourhoodContacts.isEmpty()) {
            item {
                EmptyContactsHint("पड़ोसियों को यहाँ जोड़ें")
            }
        }

        items(uiState.neighbourhoodContacts, key = { it.id }) { contact ->
            ContactCard(
                contact = contact,
                onCall = {
                    if (callPermission.status.isGranted) {
                        context.startActivity(
                            Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.phoneNumber}"))
                        )
                    } else callPermission.launchPermissionRequest()
                },
                onEdit = { viewModel.showEditDialog(contact) },
                onDelete = { viewModel.deleteContact(contact) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── Community Alert Broadcast ──────────────────────────────────────
        if (uiState.neighbourhoodContacts.isNotEmpty()) {
            item {
                CommunityAlertCard(
                    message = uiState.communityAlertMessage,
                    onMessageChange = viewModel::updateCommunityMessage,
                    onSend = { viewModel.requestCommunityAlert() }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    // ── Add / Edit dialog ──────────────────────────────────────────────────
    if (uiState.showAddDialog && uiState.editContact != null) {
        ContactEditDialog(
            contact = uiState.editContact!!,
            onSave = viewModel::saveContact,
            onDismiss = viewModel::dismissDialog
        )
    }

    // ── Community Alert confirmation ───────────────────────────────────────
    if (uiState.showCommunityConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCommunityConfirm,
            icon = { Icon(Icons.Filled.Send, contentDescription = null) },
            title = { Text("Community Alert भेजें?") },
            text = {
                Text(
                    "यह SMS ${uiState.neighbourhoodContacts.size} लोगों को भेजा जाएगा:\n\n" +
                        "\"${uiState.communityAlertMessage}\""
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (smsPermission.status.isGranted) {
                        SmsHelper.sendBulkSms(
                            uiState.neighbourhoodContacts.map { it.phoneNumber },
                            uiState.communityAlertMessage
                        )
                    } else smsPermission.launchPermissionRequest()
                    viewModel.dismissCommunityConfirm()
                }) { Text("भेजें") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCommunityConfirm) { Text("रद्द करें") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        FilledTonalIconButton(onClick = onAdd) {
            Icon(Icons.Filled.PersonAdd, contentDescription = "Add contact")
        }
    }
}

@Composable
private fun EmptyContactsHint(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(contact.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${contact.phoneNumber}  •  ${contact.relationship}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            // Call button
            IconButton(onClick = onCall) {
                Icon(Icons.Filled.Call, contentDescription = "Call",
                    tint = MaterialTheme.colorScheme.primary)
            }
            // Edit
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            // Delete
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete करें?") },
            text = { Text("${contact.name} को हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("हाँ, Delete करें") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("रद्द करें") }
            }
        )
    }
}

@Composable
private fun CommunityAlertCard(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Community Alert भेजें", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Alert Message") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSend, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Neighbourhood Group को Alert भेजें")
            }
        }
    }
}

@Composable
private fun ContactEditDialog(
    contact: EmergencyContact,
    onSave: (EmergencyContact) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(contact.name) }
    var phone by remember { mutableStateOf(contact.phoneNumber) }
    var relationship by remember { mutableStateOf(contact.relationship) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (contact.id == 0) "नया Contact जोड़ें" else "Contact Edit करें")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("नाम (Name)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("फोन नंबर") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("रिश्ता (जैसे: Papa, Friend, Neighbour)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(contact.copy(name = name.trim(), phoneNumber = phone.trim(),
                            relationship = relationship.trim()))
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) { Text("Save करें") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}
