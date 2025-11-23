package com.example.kidosmartbadge.data

data class PairingCodeData(
    val status: String = "",
    val timestamp: Long = 0,
    val rfid_uid: String? = null
)

data class Child(
    val name: String = "",
    val rfidUid: String = ""
)