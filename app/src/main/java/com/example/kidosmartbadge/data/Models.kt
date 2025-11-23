package com.example.kidosmartbadge.data

// Data class untuk data anak yang sudah ada di Pairing.kt
// data class Child(val name: String = "", val rfidUid: String = "")

data class AttendanceRecord(
    val uid: String = "",
    val waktu: Long = 0,
    val tipe: String = ""
)

data class BadgeRecord(
    val uid: String = "",
    val badge_name: String = "",
    val status: String = ""
)
