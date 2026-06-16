package com.kawach.app.ui.log

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kawach.app.data.local.entity.IncidentLogEntry
import com.kawach.app.data.repository.IncidentRepository
import com.kawach.app.util.LocationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class LogUiState(
    val entries: List<IncidentLogEntry> = emptyList(),
    val showAddDialog: Boolean = false,
    val editEntry: IncidentLogEntry? = null,
    val isFetchingLocation: Boolean = false,
    val exportMessage: String? = null
)

class IncidentLogViewModel(
    private val repository: IncidentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allEntries.collect { entries ->
                _uiState.update { it.copy(entries = entries) }
            }
        }
    }

    fun openAddDialog(context: Context) {
        val entry = IncidentLogEntry(timestamp = System.currentTimeMillis(), description = "")
        _uiState.update { it.copy(showAddDialog = true, editEntry = entry) }
        // Auto-fill location
        fetchLocationForEntry(context, entry)
    }

    fun openEditDialog(entry: IncidentLogEntry) {
        _uiState.update { it.copy(showAddDialog = true, editEntry = entry) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showAddDialog = false, editEntry = null) }
    }

    fun updateEditEntry(entry: IncidentLogEntry) {
        _uiState.update { it.copy(editEntry = entry) }
    }

    fun saveEntry() {
        val entry = _uiState.value.editEntry ?: return
        viewModelScope.launch {
            if (entry.id == 0) repository.add(entry) else repository.update(entry)
        }
        dismissDialog()
    }

    fun deleteEntry(entry: IncidentLogEntry) {
        viewModelScope.launch { repository.delete(entry) }
    }

    fun exportLog(context: Context) {
        viewModelScope.launch {
            val entries = _uiState.value.entries
            if (entries.isEmpty()) {
                _uiState.update { it.copy(exportMessage = "No entries to export") }
                return@launch
            }

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val sb = StringBuilder("=== Kawach Incident Log ===\n\n")
            entries.forEach { e ->
                sb.appendLine("Date: ${sdf.format(Date(e.timestamp))}")
                if (e.latitude != null && e.longitude != null) {
                    sb.appendLine("Location: ${LocationHelper.toMapsLink(e.latitude, e.longitude)}")
                }
                sb.appendLine("Description: ${e.description}")
                if (e.triggeredBySOS) sb.appendLine("(Auto-logged via SOS)")
                sb.appendLine("─────────────────────────")
            }

            try {
                val dir = File(context.getExternalFilesDir(null), "exports")
                dir.mkdirs()
                val file = File(dir, "kawach_log_${System.currentTimeMillis()}.txt")
                file.writeText(sb.toString())

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Incident Log"))
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }

    private fun fetchLocationForEntry(context: Context, base: IncidentLogEntry) {
        _uiState.update { it.copy(isFetchingLocation = true) }
        viewModelScope.launch {
            try {
                val loc = LocationHelper.getCurrentLocation(context)
                val updated = base.copy(latitude = loc?.latitude, longitude = loc?.longitude)
                // Only update if dialog still open for this same entry
                if (_uiState.value.editEntry?.id == updated.id) {
                    _uiState.update { it.copy(editEntry = updated, isFetchingLocation = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isFetchingLocation = false) }
            }
        }
    }

    class Factory(private val repository: IncidentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            IncidentLogViewModel(repository) as T
    }
}
