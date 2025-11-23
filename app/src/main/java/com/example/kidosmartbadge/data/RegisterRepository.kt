package com.example.kidosmartbadge.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RegisterRepository {

    private val db = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    private val auth = FirebaseAuth.getInstance()

    suspend fun registerUser(cardUid: String, childName: String, email: String, password: String) {
        // 1. Check if card is available
        val formattedCardUid = cardUid.replace(" ", "").uppercase()
        val cardRef = db.child("unclaimed_cards").child(formattedCardUid)
        val cardSnapshot = cardRef.get().await()

        if (!cardSnapshot.exists() || cardSnapshot.getValue(String::class.java) != "available") {
            throw IllegalStateException("Card UID is not valid or has already been claimed.")
        }

        // 2. Create user in Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val parentUid = authResult.user?.uid ?: throw IllegalStateException("Failed to create user account.")

        // 3. Prepare all database writes in a multi-path update
        val childId = UUID.randomUUID().toString()
        val childData = Child(name = childName, rfidUid = formattedCardUid)

        val updates = mapOf(
            // a. Create user data with role
            "/users/$parentUid/role" to "Ortu",
            // b. Create child data under parent
            "/users/$parentUid/children/$childId" to childData,
            // c. Create parent mapping for the card
            "/rfid_to_parent_mapping/$formattedCardUid" to parentUid,
            // d. Mark card as claimed
            "/unclaimed_cards/$formattedCardUid" to "claimed"
        )

        // 4. Execute all writes at once
        db.updateChildren(updates).await()
    }
}