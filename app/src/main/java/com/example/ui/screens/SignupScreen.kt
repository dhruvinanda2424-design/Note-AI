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
import androidx.compose.material.icons.filled.Person
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
import com.example.ui.components.PasswordStrengthIndicatorState
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name by viewModel.signupName.collectAsState()
    val email by viewModel.signupEmail.collectAsState()
    val password by viewModel.signupPassword.collectAsState()
    val confirmPassword by viewModel.signupConfirmPassword.collectAsState()

    val nameError by viewModel.signupNameError.collectAsState()
    val emailError by viewModel.signupEmailError.collectAsState()
    val passwordError by viewModel.signupPasswordError.collectAsState()
    val confirmPasswordError by viewModel.signupConfirmPasswordError.collectAsState()
    val generalError by viewModel.signupGeneralError.collectAsState()
    val isLoading by viewModel.signupLoading.collectAsState()

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
                .testTag("auth_theme_toggle_signup")
        ) {
            Icon(
                imageVector = if (isDark == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle dark mode",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(scrollState)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(28.dp)
                .testTag("signup_card"),
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
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Sign up to track your study and work notes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // General Failure Notification Banner
            if (generalError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(14.dp)
                        .testTag("signup_error_banner")
                ) {
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Input fields
            AuthInputField(
                value = name,
                onValueChange = { viewModel.signupName.value = it },
                label = "Full Name",
                placeholder = "Jordan Dev",
                leadingIcon = Icons.Default.Person,
                error = nameError,
                testTag = "signup_name_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email Address
            AuthInputField(
                value = email,
                onValueChange = { viewModel.signupEmail.value = it },
                label = "Email Address",
                placeholder = "jordan.dev@notes.io",
                leadingIcon = Icons.Default.Email,
                error = emailError,
                testTag = "signup_email_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password
            AuthInputField(
                value = password,
                onValueChange = { viewModel.signupPassword.value = it },
                label = "Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                error = passwordError,
                isPassword = true,
                testTag = "signup_password_input"
            )

            // Interactive Password Strength State
            PasswordStrengthIndicatorState(password = password)

            Spacer(modifier = Modifier.height(10.dp))

            // Confirm Password
            AuthInputField(
                value = confirmPassword,
                onValueChange = { viewModel.signupConfirmPassword.value = it },
                label = "Confirm Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                error = confirmPasswordError,
                isPassword = true,
                testTag = "signup_confirm_password_input"
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Action Button
            AuthPrimaryButton(
                text = "Create Account",
                onClick = { viewModel.signup() },
                isLoading = isLoading,
                testTag = "signup_button"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation back to Login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clickable(onClick = onNavigateToLogin)
                        .testTag("login_redirect_link")
                )
            }
        }
    }
}
