package com.example.kidosmartbadge.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val role: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(_email.value, _password.value).await()
                val user = result.user ?: throw IllegalStateException("User not found after sign in")
                val role = checkUserRole(user.uid)
                _uiState.value = LoginUiState.Success(role)
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    private suspend fun checkUserRole(userId: String): String {
        val snapshot = database.getReference("users").child(userId).get().await()
        return snapshot.child("role").getValue(String::class.java) ?: throw IllegalStateException("User role not found.")
    }
}