package com.example.kidosmartbadge.ui.linkcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun LinkCardScreen(
    navController: NavController,
    childName: String,
    viewModel: LinkCardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val code by viewModel.code.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is LinkCardUiState.Idle, is LinkCardUiState.Error -> {
                    Text(
                        text = "Linking Card for $childName",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Go to the KIDO Station, press and hold the button for 3 seconds until you get a 6-digit code. Enter the code below.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { viewModel.onCodeChanged(it) },
                        label = { Text("6-Digit Verification Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onVerifyClicked() },
                        enabled = code.length == 6
                    ) {
                        Text("Verify & Continue")
                    }
                    if (state is LinkCardUiState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = state.message, color = Color.Red)
                    }
                }
                is LinkCardUiState.VerifyingCode -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifying code...")
                }
                is LinkCardUiState.WaitingForCard -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Code Correct! Now tap your child's card on the KIDO Station.", textAlign = TextAlign.Center)
                }
                is LinkCardUiState.Success -> {
                    Text("Success!", style = MaterialTheme.typography.headlineMedium, color = Color.Green)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("The card for $childName has been successfully linked.", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        navController.navigate("home_root") {
                            popUpTo("home_root") {
                                inclusive = true
                            }
                        }
                    }) {
                        Text("Done")
                    }
                }
            }
        }
    }
}