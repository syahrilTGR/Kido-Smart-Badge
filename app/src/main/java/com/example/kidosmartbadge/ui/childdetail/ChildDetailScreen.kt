package com.example.kidosmartbadge.ui.childdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailScreen(
    childName: String,
    childRfidUid: String,
    viewModel: ChildDetailViewModel = viewModel()
) {
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val badgeRecords by viewModel.badgeRecords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(childName) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("RFID UID: $childRfidUid", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(24.dp))

            // Attendance Section
            Text("Riwayat Kehadiran", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (attendanceRecords.isEmpty()) {
                    item { Text("Belum ada data kehadiran.") }
                } else {
                    items(attendanceRecords) { record ->
                        ListItem(headlineContent = { Text(formatTimestamp(record.waktu)) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Badges Section
            Text("Pencapaian", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (badgeRecords.isEmpty()) {
                    item { Text("Belum ada pencapaian.") }
                } else {
                    items(badgeRecords) { record ->
                        ListItem(headlineContent = { Text(record.badge_name) })
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}