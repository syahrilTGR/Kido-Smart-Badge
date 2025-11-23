package com.example.kidosmartbadge.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidosmartbadge.data.RegisterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {

    private val repository = RegisterRepository()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    val cardUid = MutableStateFlow("")
    val childName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun onRegisterClicked() {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                repository.registerUser(
                    cardUid.value,
                    childName.value,
                    email.value,
                    password.value
                )
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}