package com.aicaller.app.ui.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.data.local.entities.CallRecordEntity
import com.aicaller.app.data.local.entities.ContactInsightEntity
import com.aicaller.app.data.repository.CallRepository
import com.aicaller.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    contactRepository: ContactRepository,
    callRepository: CallRepository
) : ViewModel() {

    val phoneNumber: String = URLDecoder.decode(
        checkNotNull(savedStateHandle.get<String>("phoneNumber")),
        "UTF-8"
    )

    val insight: StateFlow<ContactInsightEntity?> = contactRepository.observeInsight(phoneNumber)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val callHistory: StateFlow<List<CallRecordEntity>> = callRepository.observeCallRecordsForNumber(phoneNumber)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
