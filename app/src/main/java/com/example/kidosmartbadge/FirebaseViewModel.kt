package com.example.kidosmartbadge

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class UiState {
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    object Empty : UiState()
}

class FirebaseViewModel : ViewModel() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val _uiState = MutableStateFlow<UiState>(UiState.Empty)
    val uiState: StateFlow<UiState> = _uiState

    fun initialize() {
        auth = Firebase.auth
        database = Firebase.database("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }

    fun signUp(email: String, password: String) {
        _uiState.value = UiState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = UiState.Success("Sign up successful")
                    Log.d("FirebaseViewModel", "createUserWithEmail:success")
                    val user = auth.currentUser
                } else {
                    _uiState.value = UiState.Error("Sign up failed: ${task.exception?.message}")
                    Log.w("FirebaseViewModel", "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun signIn(email: String, password: String) {
        _uiState.value = UiState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = UiState.Success("Sign in successful")
                    Log.d("FirebaseViewModel", "signInWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        checkUserRole(it.uid)
                    }
                } else {
                    _uiState.value = UiState.Error("Sign in failed: ${task.exception?.message}")
                    Log.w("FirebaseViewModel", "signInWithEmail:failure", task.exception)
                }
            }
    }

    private fun checkUserRole(userId: String) {
        val userRef = database.getReference("users").child(userId)
        userRef.get().addOnSuccessListener {
            val role = it.child("role").getValue(String::class.java)
            val childUid = it.child("child_uid").getValue(String::class.java)
            when (role) {
                "Orang Tua" -> {
                    _uiState.value = UiState.Success("Role: Orang Tua")
                    Log.d("FirebaseViewModel", "User is Orang Tua")
                    childUid?.let { uid ->
                        getAbsensiAnak(uid)
                        getBadgesAnak(uid)
                    }
                }
                "Mentor" -> {
                    _uiState.value = UiState.Success("Role: Mentor")
                    Log.d("FirebaseViewModel", "User is Mentor")
                }
                else -> {
                    _uiState.value = UiState.Error("User role not found")
                    Log.d("FirebaseViewModel", "User role not found")
                }
            }
        }.addOnFailureListener{
            _uiState.value = UiState.Error("Error getting user role: ${it.message}")
            Log.e("FirebaseViewModel", "Error getting user role", it)
        }
    }

    fun getAbsensiAnak(childUid: String) {
        val absensiRef = database.getReference("absensi")
        absensiRef.orderByChild("uid").equalTo(childUid).get()
            .addOnSuccessListener {
                Log.d("FirebaseViewModel", "Absensi: ${it.value}")
            }.addOnFailureListener {
                Log.e("FirebaseViewModel", "Error getting absensi", it)
            }
    }

    fun getBadgesAnak(childUid: String) {
        val badgesRef = database.getReference("badges_earned")
        badgesRef.orderByChild("uid").equalTo(childUid).get()
            .addOnSuccessListener {
                Log.d("FirebaseViewModel", "Badges: ${it.value}")
            }.addOnFailureListener {
                Log.e("FirebaseViewModel", "Error getting badges", it)
            }
    }

    fun getStudentName(uid: String) {
        val studentRef = database.getReference("students").child(uid)
        studentRef.get().addOnSuccessListener {
            val name = it.child("name").getValue(String::class.java)
            Log.d("FirebaseViewModel", "Student name: $name")
        }.addOnFailureListener{
            Log.e("FirebaseViewModel", "Error getting student name", it)
        }
    }


    fun approveProject(uidKartu: String, projectName: String) {
        _uiState.value = UiState.Loading
        val reference = database.getReference("approval_pending").child(uidKartu)
        reference.setValue(projectName)
            .addOnSuccessListener {
                _uiState.value = UiState.Success("Project approved successfully")
                Log.d("FirebaseViewModel", "approveProject:success")
            }
            .addOnFailureListener {
                _uiState.value = UiState.Error("Project approval failed: ${it.message}")
                Log.w("FirebaseViewModel", "approveProject:failure", it)
            }
    }
}
