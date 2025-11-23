package com.example.kidosmartbadge.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PairingRepository {

    private val database = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val auth = FirebaseAuth.getInstance()

    fun getPairingCodeData(code: String): Flow<Result<PairingCodeData>> = callbackFlow {
        val codeRef = database.getReference("pairing_codes").child(code)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(Result.failure(Exception("Code not valid or expired.")))
                    return
                }
                val data = snapshot.getValue(PairingCodeData::class.java)
                if (data == null) {
                    trySend(Result.failure(Exception("Failed to parse pairing data.")))
                } else {
                    trySend(Result.success(data))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        codeRef.addListenerForSingleValueEvent(listener)
        awaitClose { }
    }

    suspend fun signalAppVerified(code: String) {
        database.getReference("pairing_codes").child(code).child("status")
            .setValue("app_verified_waiting_for_card").await()
    }

    fun listenForPairingCompletion(code: String): Flow<String> = callbackFlow {
        val pairingRef = database.getReference("pairing_codes").child(code)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                if (status == "completed") {
                    val rfidUid = snapshot.child("rfid_uid").getValue(String::class.java)
                    if (rfidUid != null) {
                        trySend(rfidUid)
                        close() // Stop listening
                    } else {
                        close(IllegalStateException("Pairing completed but RFID UID is null."))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        pairingRef.addValueEventListener(listener)
        awaitClose { pairingRef.removeEventListener(listener) }
    }

    suspend fun finalizeBinding(rfidUid: String, childName: String, parentUid: String) {
        val formattedRfidUid = rfidUid.replace(" ", "").uppercase()
        val childId = UUID.randomUUID().toString()
        val child = Child(name = childName, rfidUid = formattedRfidUid)

        val updates = mutableMapOf<String, Any?>()
        updates["/users/$parentUid/children/$childId"] = child
        updates["/rfid_to_parent_mapping/$formattedRfidUid"] = parentUid

        database.reference.updateChildren(updates).await()
    }

    suspend fun cleanupPairingNode(code: String) {
        database.getReference("pairing_codes").child(code).removeValue().await()
    }
}