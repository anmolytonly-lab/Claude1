package com.aicaller.app.ui.dialer

import androidx.lifecycle.ViewModel
import com.aicaller.app.data.repository.SpamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DialerViewModel @Inject constructor(
    private val spamRepository: SpamRepository
) : ViewModel() {

    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()

    fun appendDigit(digit: String) {
        _dialedNumber.value += digit
    }

    fun backspace() {
        _dialedNumber.value = _dialedNumber.value.dropLast(1)
    }

    fun clear() {
        _dialedNumber.value = ""
    }
}
