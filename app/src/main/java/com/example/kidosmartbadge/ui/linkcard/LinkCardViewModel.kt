package com.example.kidosmartbadge.ui.linkcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidosmartbadge.data.PairingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
                .onEach { result ->
                    result.fold(
                        onSuccess = { data ->
                            // Validate status and timestamp
                            val currentTime = System.currentTimeMillis()
                            if (data.status != "waiting_for_app") {
                                _uiState.value = LinkCardUiState.Error("Code has already been used.")
                            } else if ((currentTime - data.timestamp) > 60000) {
                                _uiState.value = LinkCardUiState.Error("Code has expired.")
                            } else {
                                // Success, signal ESP32 and wait for card
                                signalAndWaitForCard(enteredCode)
                            }
                        },
                        onFailure = {
                            _uiState.value = LinkCardUiState.Error(it.message ?: "Unknown error")
                        }
                    )
                }
                .catch {
                    _uiState.value = LinkCardUiState.Error(it.message ?: "Flow error")
                }
                .launchIn(viewModelScope)
        }
    }

    private fun signalAndWaitForCard(code: String) {
        viewModelScope.launch {
            try {
                repository.signalAppVerified(code)
                _uiState.value = LinkCardUiState.WaitingForCard

                repository.listenForRfidUid(code)
                    .onEach { rfidUid ->
                        finalize(rfidUid, code)
                    }
                    .catch {
                         _uiState.value = LinkCardUiState.Error(it.message ?: "Error waiting for card.")
                    }
                    .launchIn(viewModelScope)

            } catch (e: Exception) {
                _uiState.value = LinkCardUiState.Error(e.message ?: "Failed to signal device.")
            }
        }
    }

    private fun finalize(rfidUid: String, code: String) {
        viewModelScope.launch {
            try {
                repository.finalizeBinding(rfidUid, childName, code)
                _uiState.value = LinkCardUiState.Success
            } catch (e: Exception) {
                _uiState.value = LinkCardUiState.Error(e.message ?: "Failed to finalize link.")
            }
        }
    }
}
