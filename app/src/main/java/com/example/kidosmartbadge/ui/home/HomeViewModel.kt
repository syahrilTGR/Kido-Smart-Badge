package com.example.kidosmartbadge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val message: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun approveProject(uidKartu: String, projectName: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                database.getReference("approval_pending").child(uidKartu).setValue(projectName).await()
                _uiState.value = HomeUiState.Success("Project approved successfully")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Project approval failed")
            }
        }
    }
}