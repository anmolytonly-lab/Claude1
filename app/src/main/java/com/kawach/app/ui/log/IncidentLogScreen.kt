package com.kawach.app.ui.log

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kawach.app.KawachApp
import com.kawach.app.data.local.entity.IncidentLogEntry
import com.kawach.app.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IncidentLogScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as KawachApp

    val viewModel: IncidentLogViewModel = viewModel(
        factory = IncidentLogViewModel.Factory(app.incidentRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (!locationPermission.status.isGranted) locationPermission.launchPermissionRequest()
                    viewModel.openAddDialog(context)
                },
                icon = { Icon(Icons.Filled.Add, "New entry") },
                text = { Text("नई Entry") }
            )
        }
    ) { innerPadding ->
        if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "अभी कोई incident log नहीं",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "+ बटन से नई entry जोड़ें",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${uiState.entries.size} entries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        TextButton(onClick = { viewModel.exportLog(context) }) {
                            Icon(Icons.Filled.IosShare, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export करें")
                        }
                    }
                }

                items(uiState.entries, key = { it.id }) { entry ->
                    IncidentEntryCard(
                        entry = entry,
                        onEdit = { viewModel.openEditDialog(entry) },
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }  // FAB clearance
            }
        }
    }

    // Add / Edit dialog
    if (uiState.showAddDialog && uiState.editEntry != null) {
        IncidentEntryDialog(
            entry = uiState.editEntry!!,
            isFetchingLocation = uiState.isFetchingLocation,
            onEntryChange = viewModel::updateEditEntry,
            onSave = viewModel::saveEntry,
            onDismiss = viewModel::dismissDialog
        )
    }

    // Export result snackbar
    if (uiState.exportMessage != null) {
        LaunchedEffect(uiState.exportMessage) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearExportMessage()
        }
    }
}

@Composable
private fun IncidentEntryCard(
    entry: IncidentLogEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (entry.triggeredBySOS) Icons.Filled.Warning else Icons.Filled.EditNote,
                    contentDescription = null,
                    tint = if (entry.triggeredBySOS)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    sdf.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit",
                        modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                entry.description.ifBlank { "(no description)" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (entry.latitude != null && entry.longitude != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "📍 ${LocationHelper.toMapsLink(entry.latitude, entry.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (entry.photoUri != null) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = entry.photoUri,
                    contentDescription = "Incident photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete करें?") },
            text = { Text("यह entry permanently हट जाएगी।") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("रद्द करें") }
            }
        )
    }
}

@Composable
private fun IncidentEntryDialog(
    entry: IncidentLogEntry,
    isFetchingLocation: Boolean,
    onEntryChange: (IncidentLogEntry) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onEntryChange(entry.copy(photoUri = it.toString())) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry.id == 0) "नई Entry" else "Entry Edit करें") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry.description,
                    onValueChange = { onEntryChange(entry.copy(description = it)) },
                    label = { Text("क्या हुआ? (संक्षेप में)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                // Location row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    if (isFetchingLocation) {
                        Text("Location मिल रही है…",
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else if (entry.latitude != null) {
                        Text(
                            "%.4f, %.4f".format(entry.latitude, entry.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("Location unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }

                // Photo picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { photoPicker.launch("image/*") }) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Photo जोड़ें")
                    }
                    if (entry.photoUri != null) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Photo attached",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                        TextButton(onClick = { onEntryChange(entry.copy(photoUri = null)) }) {
                            Text("Remove")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Save करें") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}
