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
                val data = snapshot.getValue<PairingCodeData>()
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

    fun listenForRfidUid(code: String): Flow<String> = callbackFlow {
        val rfidUidRef = database.getReference("pairing_codes").child(code).child("rfid_uid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<String>()?.let {
                    trySend(it)
                    close()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        rfidUidRef.addValueEventListener(listener)
        awaitClose { rfidUidRef.removeEventListener(listener) }
    }

    suspend fun finalizeBinding(rfidUid: String, childName: String, code: String) {
        val parentUid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val formattedRfidUid = rfidUid.replace(" ", "").uppercase()
        val childId = UUID.randomUUID().toString()
        val child = Child(name = childName, rfidUid = formattedRfidUid)

        // 1. Create child data entry
        database.getReference("users").child(parentUid).child("children").child(childId)
            .setValue(child).await()

        // 2. Create mapping
        database.getReference("rfid_to_parent_mapping").child(formattedRfidUid)
            .setValue(parentUid).await()

        // 3. Clean up
        database.getReference("pairing_codes").child(code).removeValue().await()
    }
}
