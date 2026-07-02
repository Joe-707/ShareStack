package com.sharestack.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthService {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Any email/password works for the demo
    fun login(email: String, password: String): Boolean {
        _isLoggedIn.value = true
        return true
    }

    fun signup(name: String, email: String, password: String): Boolean {
        _isLoggedIn.value = true
        return true
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}