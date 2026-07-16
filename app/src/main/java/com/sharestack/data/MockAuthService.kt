package com.sharestack.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthService {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // TEMPORARY MEMORY BANK for the MVP
    private val registeredUsers = mutableMapOf<String, String>()

    fun login(email: String, password: String): Boolean {
        _isLoggedIn.value = true
        return true
    }

    fun signup(name: String, email: String, password: String): Boolean {
        // Save the exact name they registered with!
        registeredUsers[email] = name
        _isLoggedIn.value = true
        return true
    }

    // Allows the ViewModel to look up their real name later
    fun getRegisteredName(email: String): String? {
        return registeredUsers[email]
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}