package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    testTag: String = "",
    isPassword: Boolean = false,
    placeholder: String = ""
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    text = if (placeholder.isNotEmpty()) placeholder else label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "$label icon",
                )
            },
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.testTag("${testTag}_toggle")
                    ) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                }
            } else null,
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDark) Color(0xFF2E3033) else Color(0xFFEEF0F8),
                unfocusedContainerColor = if (isDark) Color(0xFF2E3033) else Color(0xFFEEF0F8),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            singleLine = true
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(start = 8.dp, top = 4.dp)
                    .testTag("${testTag}_error")
            )
        }
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(testTag),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("${testTag}_loading")
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun PasswordStrengthIndicatorState(password: String) {
    if (password.isEmpty()) return

    val strength = remember(password) {
        calculatePasswordStrength(password)
    }

    val strengthColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
            PasswordStrength.MEDIUM -> Color(0xFFF59E0B) // Amber
            PasswordStrength.STRONG -> Color(0xFF10B981) // Emerald
            PasswordStrength.UNBEATABLE -> Color(0xFF8B5CF6) // Violet
        },
        label = "StrengthColor"
    )

    val strengthProgress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> 0.25f
            PasswordStrength.MEDIUM -> 0.5f
            PasswordStrength.STRONG -> 0.75f
            PasswordStrength.UNBEATABLE -> 1.0f
        },
        label = "StrengthProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("password_strength_indicator")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strength.displayText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = strengthColor,
                modifier = Modifier.testTag("password_strength_label")
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val bars = 4
            for (i in 0 until bars) {
                val isActive = (i + 1) <= (strengthProgress * bars)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isActive) strengthColor else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Must be at least 8 characters with numbers & symbols.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

enum class PasswordStrength(val displayText: String) {
    WEAK("Weak 😟"),
    MEDIUM("Medium 😐"),
    STRONG("Strong 💪"),
    UNBEATABLE("Unbeatable 🔥")
}

private fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.length < 8) return PasswordStrength.WEAK
    var score = 0
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 1 -> PasswordStrength.WEAK
        score == 2 -> PasswordStrength.MEDIUM
        score == 3 -> PasswordStrength.STRONG
        else -> PasswordStrength.UNBEATABLE
    }
}
