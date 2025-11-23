package com.example.kidosmartbadge.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class SplashViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _authState.value = AuthState.Unauthenticated
            } else {
                try {
                    val role = checkUserRole(user.uid)
                    _authState.value = AuthState.Authenticated(role)
                } catch (e: Exception) {
                    // If role check fails, sign out user to be safe
                    auth.signOut()
                    _authState.value = AuthState.Error(e.message ?: "Failed to verify user role.")
                }
            }
        }
    }

    private suspend fun checkUserRole(userId: String): String {
        val snapshot = database.getReference("users").child(userId).get().await()
        return snapshot.child("role").getValue(String::class.java)?.trim() ?: throw IllegalStateException("User role not found.")
    }
}