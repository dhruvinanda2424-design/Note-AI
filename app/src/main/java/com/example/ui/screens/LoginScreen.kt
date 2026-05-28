package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthInputField
import com.example.ui.components.AuthPrimaryButton
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val rememberMe by viewModel.loginRememberMe.collectAsState()
    
    val emailError by viewModel.loginEmailError.collectAsState()
    val passwordError by viewModel.loginPasswordError.collectAsState()
    val generalError by viewModel.loginGeneralError.collectAsState()
    val isLoading by viewModel.loginLoading.collectAsState()
    val successMessage by viewModel.loginSuccessMessage.collectAsState()

    val isDark by viewModel.isDarkMode.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Theme Toggle Button
        IconButton(
            onClick = { viewModel.toggleDarkMode() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .testTag("auth_theme_toggle_login")
        ) {
            Icon(
                imageVector = if (isDark == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle dark mode",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 480.dp) // Perfect responsive constraint for tablet/desktop vs mobile
                .verticalScroll(scrollState)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(28.dp)
                .testTag("login_card"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimalist Logo Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("app_logo"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAlt,
                    contentDescription = "App Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Sign in to access your secure personal notes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
            )

            // Success Notification Banner
            if (successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(14.dp)
                        .testTag("success_message_banner")
                ) {
                    Text(
                        text = successMessage ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // General Failure Notification Banner
            if (generalError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(14.dp)
                        .testTag("general_error_banner")
                ) {
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Input Fields
            AuthInputField(
                value = email,
                onValueChange = {
                    viewModel.loginSuccessMessage.value = null
                    viewModel.loginEmail.value = it
                },
                label = "Email Address",
                placeholder = "jordan.dev@notes.io",
                leadingIcon = Icons.Default.Email,
                error = emailError,
                testTag = "login_email_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthInputField(
                value = password,
                onValueChange = {
                    viewModel.loginSuccessMessage.value = null
                    viewModel.loginPassword.value = it
                },
                label = "Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                error = passwordError,
                isPassword = true,
                testTag = "login_password_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remember Me and Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.loginRememberMe.value = !rememberMe }
                        .padding(4.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.loginRememberMe.value = it },
                        modifier = Modifier.testTag("remember_me_checkbox")
                    )
                    Text(
                        text = "Remember Me",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clickable {
                            viewModel.loginSuccessMessage.value = null
                            onNavigateToForgotPassword()
                        }
                        .padding(4.dp)
                        .testTag("forgot_password_link")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            AuthPrimaryButton(
                text = "Log In",
                onClick = { viewModel.login() },
                isLoading = isLoading,
                testTag = "login_button"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation to Signup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clickable {
                            viewModel.loginSuccessMessage.value = null
                            onNavigateToSignup()
                        }
                        .testTag("signup_redirect_link")
                )
            }
        }
    }
}
