package com.example.kidosmartbadge.ui.linkcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidosmartbadge.data.PairingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

sealed class LinkCardUiState {
    object Idle : LinkCardUiState()
    object VerifyingCode : LinkCardUiState()
    object WaitingForCard : LinkCardUiState()
    data class Error(val message: String) : LinkCardUiState()
    object Success : LinkCardUiState()
}

class LinkCardViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val childName: String = savedStateHandle.get<String>("childName")!!
    private val repository = PairingRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<LinkCardUiState>(LinkCardUiState.Idle)
    val uiState: StateFlow<LinkCardUiState> = _uiState.asStateFlow()

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    fun onCodeChanged(newCode: String) {
        _code.value = newCode
    }

    fun onVerifyClicked() {
        viewModelScope.launch {
            _uiState.value = LinkCardUiState.VerifyingCode
            val enteredCode = _code.value
            repository.getPairingCodeData(enteredCode)
                .catch {
                    _uiState.value = LinkCardUiState.Error(it.message ?: "Flow error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            val currentTime = System.currentTimeMillis()
                            if (data.status != "waiting_for_app") {
                                _uiState.value = LinkCardUiState.Error("Code has already been used.")
                            } else if ((currentTime - data.timestamp) > 60000) {
                                _uiState.value = LinkCardUiState.Error("Code has expired.")
                            } else {
                                signalAndWaitForCard(enteredCode)
                            }
                        },
                        onFailure = {
                            _uiState.value = LinkCardUiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }

    private fun signalAndWaitForCard(code: String) {
        viewModelScope.launch {
            try {
                repository.signalAppVerified(code)
                _uiState.value = LinkCardUiState.WaitingForCard

                repository.listenForPairingCompletion(code)
                    .catch {
                        _uiState.value = LinkCardUiState.Error(it.message ?: "Error waiting for card.")
                    }
                    .collect { rfidUid ->
                        finalize(rfidUid, code)
                    }

            } catch (e: Exception) {
                _uiState.value = LinkCardUiState.Error(e.message ?: "Failed to signal device.")
            }
        }
    }

    private fun finalize(rfidUid: String, code: String) {
        viewModelScope.launch {
            try {
                val parentUid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
                repository.finalizeBinding(rfidUid, childName, parentUid)
                repository.cleanupPairingNode(code)
                _uiState.value = LinkCardUiState.Success
            } catch (e: Exception) {
                _uiState.value = LinkCardUiState.Error(e.message ?: "Failed to finalize link.")
            }
        }
    }
}