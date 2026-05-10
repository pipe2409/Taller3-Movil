package com.example.taller3_movil.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller3_movil.data.model.User
import com.example.taller3_movil.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var name by mutableStateOf("")
    var identification by mutableStateOf("")
    var phone by mutableStateOf("")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val uid = repository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                val user = repository.getUserData(uid)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            if (result.isSuccess) {
                checkCurrentUser()
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = User(
                name = name,
                identification = identification,
                email = email,
                phone = phone
            )
            val result = repository.register(user, password)
            if (result.isSuccess) {
                checkCurrentUser()
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            val result = repository.updateUserData(updatedUser)
            if (result.isSuccess) {
                _currentUser.value = updatedUser
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
