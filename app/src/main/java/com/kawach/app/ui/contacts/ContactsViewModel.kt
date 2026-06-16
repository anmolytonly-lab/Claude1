package com.kawach.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kawach.app.data.local.entity.EmergencyContact
import com.kawach.app.data.repository.ContactRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ContactsUiState(
    val personalContacts: List<EmergencyContact> = emptyList(),
    val neighbourhoodContacts: List<EmergencyContact> = emptyList(),
    val showAddDialog: Boolean = false,
    val editContact: EmergencyContact? = null,
    val communityAlertMessage: String = "Safety Alert: कृपया सावधान रहें और alert रहें।",
    val showCommunityConfirm: Boolean = false
)

class ContactsViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.personalContacts.collect { list ->
                _uiState.update { it.copy(personalContacts = list) }
            }
        }
        viewModelScope.launch {
            repository.neighbourhoodContacts.collect { list ->
                _uiState.update { it.copy(neighbourhoodContacts = list) }
            }
        }
    }

    fun showAddDialog(isNeighbourhood: Boolean = false) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editContact = EmergencyContact(
                    name = "", phoneNumber = "", relationship = "",
                    isNeighbourhoodGroup = isNeighbourhood
                )
            )
        }
    }

    fun showEditDialog(contact: EmergencyContact) {
        _uiState.update { it.copy(showAddDialog = true, editContact = contact) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showAddDialog = false, editContact = null) }
    }

    fun saveContact(contact: EmergencyContact) {
        viewModelScope.launch {
            if (contact.id == 0) repository.add(contact) else repository.update(contact)
        }
        dismissDialog()
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch { repository.delete(contact) }
    }

    fun updateCommunityMessage(msg: String) {
        _uiState.update { it.copy(communityAlertMessage = msg) }
    }

    fun requestCommunityAlert() {
        _uiState.update { it.copy(showCommunityConfirm = true) }
    }

    fun dismissCommunityConfirm() {
        _uiState.update { it.copy(showCommunityConfirm = false) }
    }

    class Factory(private val repository: ContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ContactsViewModel(repository) as T
    }
}
