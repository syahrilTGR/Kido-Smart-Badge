package com.example.kidosmartbadge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidosmartbadge.data.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val message: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance("https://snapcal-d544a-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children

    private val _allChildren = MutableStateFlow<List<Child>>(emptyList())
    val allChildren: StateFlow<List<Child>> = _allChildren

    private val _modules = MutableStateFlow<List<String>>(emptyList())
    val modules: StateFlow<List<String>> = _modules

    init {
        fetchModules()
    }

    fun fetchModules() {
        val modulesRef = database.getReference("modules")
        modulesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val moduleList = snapshot.getValue<List<String>>() ?: emptyList()
                _modules.value = moduleList
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = HomeUiState.Error("Failed to fetch modules: ${error.message}")
            }
        })
    }

    fun fetchChildren() {
        val userId = auth.currentUser?.uid ?: return
        val childrenRef = database.getReference("users").child(userId).child("children")

        childrenRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val childList = mutableListOf<Child>()
                snapshot.children.forEach { childSnapshot ->
                    childSnapshot.getValue(Child::class.java)?.let { child ->
                        childList.add(child)
                    }
                }
                _children.value = childList
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = HomeUiState.Error("Failed to fetch children: ${error.message}")
            }
        })
    }

    fun fetchAllChildren() {
        val usersRef = database.getReference("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allChildrenList = mutableListOf<Child>()
                snapshot.children.forEach { userSnapshot ->
                    // We only care about the children of "Ortu"
                    if (userSnapshot.child("role").getValue(String::class.java) == "Ortu") {
                        val childrenSnapshot = userSnapshot.child("children")
                        childrenSnapshot.children.forEach { childSnapshot ->
                            childSnapshot.getValue(Child::class.java)?.let { child ->
                                allChildrenList.add(child)
                            }
                        }
                    }
                }
                _allChildren.value = allChildrenList
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = HomeUiState.Error("Failed to fetch all children: ${error.message}")
            }
        })
    }

    fun signOut() {
        auth.signOut()
    }

    fun approveProject(uidKartu: String, projectName: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                database.getReference("approval_pending").child(uidKartu).setValue(projectName).await()
                _uiState.value = HomeUiState.Success("Project approved successfully")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Project approval failed")
            }
        }
    }
}