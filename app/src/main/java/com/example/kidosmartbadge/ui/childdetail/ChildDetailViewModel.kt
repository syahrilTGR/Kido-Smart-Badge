package com.example.kidosmartbadge.ui.childdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.kidosmartbadge.data.AttendanceRecord
import com.example.kidosmartbadge.data.BadgeRecord
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChildDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val rfidUid: String = savedStateHandle.get<String>("childRfidUid")!!
    private val database = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords

    private val _badgeRecords = MutableStateFlow<List<BadgeRecord>>(emptyList())
    val badgeRecords: StateFlow<List<BadgeRecord>> = _badgeRecords

    init {
        fetchAttendance()
        fetchBadges()
    }

    private fun fetchAttendance() {
        android.util.Log.d("ChildDetailVM", "Fetching attendance for UID: $rfidUid")
        val attendanceRef = database.getReference("absensi").orderByChild("uid").equalTo(rfidUid)
        attendanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    android.util.Log.d("ChildDetailVM", "Attendance snapshot does not exist.")
                    _attendanceRecords.value = emptyList()
                    return
                }
                val records = snapshot.children.mapNotNull { it.getValue(AttendanceRecord::class.java) }
                _attendanceRecords.value = records.sortedByDescending { it.waktu }
                android.util.Log.d("ChildDetailVM", "Successfully fetched ${records.size} attendance records.")
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ChildDetailVM", "Failed to fetch attendance: ${error.message}", error.toException())
            }
        })
    }

    private fun fetchBadges() {
        val badgesRef = database.getReference("badges_earned").orderByChild("uid").equalTo(rfidUid)
        badgesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = snapshot.children.mapNotNull { it.getValue(BadgeRecord::class.java) }
                _badgeRecords.value = records
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}