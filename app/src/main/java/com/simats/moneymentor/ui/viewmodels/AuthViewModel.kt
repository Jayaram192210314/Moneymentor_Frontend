package com.simats.moneymentor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.moneymentor.data.AuthRepository
import com.simats.moneymentor.data.model.LoginRequest
import com.simats.moneymentor.data.model.RegisterRequest
import com.simats.moneymentor.data.model.ForgotPasswordRequest
import com.simats.moneymentor.data.model.VerifyOtpRequest
import com.simats.moneymentor.data.model.ResetPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private var currentUserEmail: String = ""

    var loginResponse: com.simats.moneymentor.data.model.LoginResponse? = null
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(LoginRequest(email, password))
            result.onSuccess {
                loginResponse = it
                _authState.value = AuthState.Success(it.message)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String, phone: String, dob: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(RegisterRequest(fullName, email, dob, phone, password, confirmPassword))
            result.onSuccess {
                _authState.value = AuthState.Success(it.message)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            currentUserEmail = email
            val result = repository.forgotPassword(ForgotPasswordRequest(email))
            result.onSuccess {
                _authState.value = AuthState.Success(it.message)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Failed to send reset code")
            }
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.verifyOtp(VerifyOtpRequest(otp))
            result.onSuccess {
                _authState.value = AuthState.Success(it.message)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Verification failed")
            }
        }
    }

    fun resetPassword(password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.resetPassword(ResetPasswordRequest(currentUserEmail, password, confirmPassword))
            result.onSuccess {
                _authState.value = AuthState.Success(it.message)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Failed to reset password")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
