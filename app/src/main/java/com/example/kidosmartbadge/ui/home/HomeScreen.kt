package com.example.kidosmartbadge.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kidosmartbadge.data.Child

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    role: String,
    viewModel: HomeViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kido Smart Badge") },
                actions = {
                    IconButton(onClick = {
                        viewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("home_root") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                ParentView(navController, viewModel)
            } else if (role.trim() == "Mentor") {
                MentorView(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentView(navController: NavController, viewModel: HomeViewModel) {
    // Fetch children when the composable is first launched
    LaunchedEffect(Unit) {
        viewModel.fetchChildren()
    }

    val children by viewModel.children.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = { navController.navigate("add_child") }) {
            Text("Link a New Child's Card")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (children.isNotEmpty()) {
            Text("Linked Children", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(children) { child ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("child_detail/${child.name}/${child.rfidUid}")
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = child.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorView(viewModel: HomeViewModel) {
    // Fetch all children when the composable is first launched
    LaunchedEffect(Unit) {
        viewModel.fetchAllChildren()
    }

    val allChildren by viewModel.allChildren.collectAsState()
    var childExpanded by remember { mutableStateOf(false) }
    var selectedChild by remember { mutableStateOf<Child?>(null) }

    val modules by viewModel.modules.collectAsState() // Collect modules from ViewModel
    var moduleExpanded by remember { mutableStateOf(false) }
    var selectedModule by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Approve Project", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown for selecting a child
        ExposedDropdownMenuBox(
            expanded = childExpanded,
            onExpandedChange = { childExpanded = !childExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedChild?.name ?: "Select a Child",
                onValueChange = {},
                readOnly = true,
                label = { Text("Child") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = childExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = childExpanded,
                onDismissRequest = { childExpanded = false }
            ) {
                allChildren.forEach { child ->
                    DropdownMenuItem(
                        text = { Text(child.name) },
                        onClick = {
                            selectedChild = child
                            childExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown for selecting a module
        ExposedDropdownMenuBox(
            expanded = moduleExpanded,
            onExpandedChange = { moduleExpanded = !moduleExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedModule ?: "Select a Module",
                onValueChange = {},
                readOnly = true,
                label = { Text("Module") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moduleExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = moduleExpanded,
                onDismissRequest = { moduleExpanded = false }
            ) {
                modules.forEach { module ->
                    DropdownMenuItem(
                        text = { Text(module) },
                        onClick = {
                            selectedModule = module
                            moduleExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                selectedChild?.let { child ->
                    selectedModule?.let { module ->
                        viewModel.approveProject(child.rfidUid, module)
                    }
                }
            },
            enabled = selectedChild != null && selectedModule != null
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