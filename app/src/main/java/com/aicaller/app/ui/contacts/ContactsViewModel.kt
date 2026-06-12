package com.aicaller.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.data.local.entities.ContactInsightEntity
import com.aicaller.app.data.repository.Contact
import com.aicaller.app.data.repository.ContactRepository
import com.aicaller.app.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactUiModel(
    val contact: Contact,
    val insight: ContactInsightEntity?
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())

    val uiModels: StateFlow<List<ContactUiModel>> = combine(
        _contacts,
        contactRepository.observeInsights()
    ) { contacts, insights ->
        val byNumber = insights.associateBy { it.phoneNumber }
        contacts.map { contact ->
            ContactUiModel(contact, byNumber[PhoneNumberUtils.normalize(contact.phoneNumber)])
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _contacts.value = contactRepository.getDeviceContacts()
        }
    }
}
