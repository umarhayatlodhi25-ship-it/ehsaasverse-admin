package com.lodhidevelop.ehsaasverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val user = auth.currentUser
                _currentUser.value = user
                checkAdminStatus(user)
                
                auth.addAuthStateListener { firebaseAuth ->
                    val updatedUser = firebaseAuth.currentUser
                    _currentUser.value = updatedUser
                    checkAdminStatus(updatedUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkAdminStatus(user: FirebaseUser?) {
        if (user != null) {
            // Hardcoded check for immediate admin access for these specific emails
            val adminEmails = listOf("admin@ehsaasverse.com", "admin@lodhidevelop.com", "lodhitools@gmail.com")
            if (user.email?.lowercase() in adminEmails) {
                _isAdmin.value = true
                return
            }

            viewModelScope.launch {
                try {
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    val role = doc.getString("role")
                    _isAdmin.value = role == "admin"
                } catch (e: Exception) {
                    _isAdmin.value = false
                }
            }
        } else {
            _isAdmin.value = false
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun signup(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user
                if (user != null) {
                    val role = if (email.lowercase() == "admin@ehsaasverse.com") "admin" else "user"
                    val userData = hashMapOf(
                        "email" to email,
                        "role" to role
                    )
                    firestore.collection("users").document(user.uid).set(userData).await()
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Signup failed")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
