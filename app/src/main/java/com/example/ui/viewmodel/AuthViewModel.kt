package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.model.User
import com.example.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- DARK MODE THEME STATE ---
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun initTheme(systemDefaultIsDark: Boolean) {
        if (_isDarkMode.value == null) {
            _isDarkMode.value = sessionManager.isDarkModeEnabled(systemDefaultIsDark)
        }
    }

    fun toggleDarkMode() {
        val current = _isDarkMode.value ?: false
        val next = !current
        _isDarkMode.value = next
        sessionManager.setDarkModeEnabled(next)
    }

    // --- Shared Navigation Events ---
    private val _navigationEvents = MutableSharedFlow<AuthNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    // --- LOGIN FORM STATE ---
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val loginRememberMe = MutableStateFlow(true)
    val loginSuccessMessage = MutableStateFlow<String?>(null)

    // Login Errors
    val loginEmailError = MutableStateFlow<String?>(null)
    val loginPasswordError = MutableStateFlow<String?>(null)
    val loginGeneralError = MutableStateFlow<String?>(null)
    val loginLoading = MutableStateFlow(false)

    // --- SIGNUP FORM STATE ---
    val signupName = MutableStateFlow("")
    val signupEmail = MutableStateFlow("")
    val signupPassword = MutableStateFlow("")
    val signupConfirmPassword = MutableStateFlow("")

    // Signup Errors
    val signupNameError = MutableStateFlow<String?>(null)
    val signupEmailError = MutableStateFlow<String?>(null)
    val signupPasswordError = MutableStateFlow<String?>(null)
    val signupConfirmPasswordError = MutableStateFlow<String?>(null)
    val signupGeneralError = MutableStateFlow<String?>(null)
    val signupLoading = MutableStateFlow(false)

    // --- FORGOT PASSWORD Form State ---
    val resetEmail = MutableStateFlow("")
    val resetEmailError = MutableStateFlow<String?>(null)
    val resetSuccessMessage = MutableStateFlow<String?>(null)
    val resetLoading = MutableStateFlow(false)

    // Email Pattern
    private val emailPattern = Pattern.compile(
        "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    )

    private fun isValidEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    // --- Actions ---

    fun login() {
        val email = loginEmail.value.trim()
        val password = loginPassword.value

        var hasError = false
        // Validate
        if (email.isEmpty()) {
            loginEmailError.value = "Email is required"
            hasError = true
        } else if (!isValidEmail(email)) {
            loginEmailError.value = "Enter a valid email address"
            hasError = true
        } else {
            loginEmailError.value = null
        }

        if (password.isEmpty()) {
            loginPasswordError.value = "Password is required"
            hasError = true
        } else {
            loginPasswordError.value = null
        }

        if (hasError) return

        viewModelScope.launch {
            loginLoading.value = true
            loginGeneralError.value = null
            
            // Artificial network/API Delay for ultra-realistic feeling loading states
            delay(1200)

            val result = com.example.data.remote.SupabaseAuth.signIn(email, password)
            result.onSuccess { (token, name) ->
                // Store session
                sessionManager.saveSession(email, name, token, loginRememberMe.value)
                
                // Clear form
                clearLoginForm()
                
                // Route navigation
                _navigationEvents.emit(AuthNavigationEvent.NavigateToDashboard)
            }.onFailure { exception ->
                loginGeneralError.value = exception.message ?: "Authentication failed"
            }
            loginLoading.value = false
        }
    }

    fun signup() {
        val name = signupName.value.trim()
        val email = signupEmail.value.trim()
        val password = signupPassword.value
        val confirmPassword = signupConfirmPassword.value

        var hasError = false

        if (name.isEmpty()) {
            signupNameError.value = "Full Name is required"
            hasError = true
        } else {
            signupNameError.value = null
        }

        if (email.isEmpty()) {
            signupEmailError.value = "Email is required"
            hasError = true
        } else if (!isValidEmail(email)) {
            signupEmailError.value = "Enter a valid email address"
            hasError = true
        } else {
            signupEmailError.value = null
        }

        if (password.isEmpty()) {
            signupPasswordError.value = "Password is required"
            hasError = true
        } else if (password.length < 8) {
            signupPasswordError.value = "Password must be at least 8 characters"
            hasError = true
        } else {
            signupPasswordError.value = null
        }

        if (confirmPassword.isEmpty()) {
            signupConfirmPasswordError.value = "Please confirm your password"
            hasError = true
        } else if (password != confirmPassword) {
            signupConfirmPasswordError.value = "Passwords do not match"
            hasError = true
        } else {
            signupConfirmPasswordError.value = null
        }

        if (hasError) return

        viewModelScope.launch {
            signupLoading.value = true
            signupGeneralError.value = null
            
            // Delay for realistic feedback
            delay(1200)

            val result = com.example.data.remote.SupabaseAuth.signUp(email, password, name)
            
            result.onSuccess {
                // Pre-fill email in Login form and show success message
                loginEmail.value = email
                loginSuccessMessage.value = "Your account has been created. Please check your email and verify your address before logging in."

                // Clear state
                clearSignupForm()
                
                // Route navigation to Login page (NavigateToLoginWithSuccess)
                _navigationEvents.emit(AuthNavigationEvent.NavigateToLoginWithSuccess)
            }.onFailure { exception ->
                signupGeneralError.value = exception.message ?: "Registration failed"
            }
            signupLoading.value = false
        }
    }

    fun sendResetLink() {
        val email = resetEmail.value.trim()

        if (email.isEmpty()) {
            resetEmailError.value = "Email is required"
            return
        } else if (!isValidEmail(email)) {
            resetEmailError.value = "Enter a valid email address"
            return
        } else {
            resetEmailError.value = null
        }

        viewModelScope.launch {
            resetLoading.value = true
            resetSuccessMessage.value = null
            
            delay(1000)
            
            // Check if user exists
            val existing = userRepository.getUserByEmail(email)
            if (existing != null) {
                resetSuccessMessage.value = "Reset link has been successfully sent to $email."
                resetEmail.value = ""
            } else {
                resetEmailError.value = "No account found with this email"
            }
            resetLoading.value = false
        }
    }

    fun logout() {
        sessionManager.clearSession()
        viewModelScope.launch {
            _navigationEvents.emit(AuthNavigationEvent.NavigateToLogin)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isUserLoggedIn()
    }

    fun getCurrentUserName(): String {
        return sessionManager.getCurrentUserName()
    }

    fun getCurrentUserEmail(): String {
        return sessionManager.getCurrentUserEmail() ?: ""
    }

    private fun clearLoginForm() {
        loginEmail.value = ""
        loginPassword.value = ""
        loginEmailError.value = null
        loginPasswordError.value = null
        loginGeneralError.value = null
    }

    private fun clearSignupForm() {
        signupName.value = ""
        signupEmail.value = ""
        signupPassword.value = ""
        signupConfirmPassword.value = ""
        signupNameError.value = null
        signupEmailError.value = null
        signupPasswordError.value = null
        signupConfirmPasswordError.value = null
        signupGeneralError.value = null
    }

    fun clearResetForm() {
        resetEmail.value = ""
        resetEmailError.value = null
        resetSuccessMessage.value = null
    }
}

sealed interface AuthNavigationEvent {
    object NavigateToDashboard : AuthNavigationEvent
    object NavigateToLogin : AuthNavigationEvent
    object NavigateToLoginWithSuccess : AuthNavigationEvent
}

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
