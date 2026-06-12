package com.aicaller.app.ui.spam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicaller.app.data.local.entities.SpamNumberEntity
import com.aicaller.app.data.repository.SpamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpamListViewModel @Inject constructor(
    private val spamRepository: SpamRepository
) : ViewModel() {

    val spamNumbers: StateFlow<List<SpamNumberEntity>> = spamRepository.observeSpamList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unblock(phoneNumber: String) {
        viewModelScope.launch {
            spamRepository.unblock(phoneNumber)
        }
    }
}
