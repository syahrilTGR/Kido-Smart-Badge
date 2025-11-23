package com.example.kidosmartbadge.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController,
    role: String,
    viewModel: HomeViewModel = viewModel()
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, $role!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (role.trim() == "Ortu") {
                ParentView(navController)
            } else if (role.trim() == "Mentor") {
                MentorView(viewModel)
            }
        }
    }
}

@Composable
fun ParentView(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = { navController.navigate("add_child") }) {
            Text("Link a New Child's Card")
        }
        // TODO: Display list of linked children here
    }
}

@Composable
fun MentorView(viewModel: HomeViewModel) {
    var uidKartu by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Approve Project", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uidKartu,
            onValueChange = { uidKartu = it },
            label = { Text("Card UID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Project Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.approveProject(uidKartu, projectName) },
            enabled = uidKartu.isNotBlank() && projectName.isNotBlank()
        ) {
            Text("Approve")
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (val state = uiState) {
            is HomeUiState.Loading -> CircularProgressIndicator()
            is HomeUiState.Success -> Text(text = state.message, color = Color.Green)
            is HomeUiState.Error -> Text(text = state.message, color = Color.Red)
            else -> {}
        }
    }
}