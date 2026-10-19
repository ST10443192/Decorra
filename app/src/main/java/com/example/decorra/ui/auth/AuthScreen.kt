package com.example.decorra.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.ui.components.DecorraTextField
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Sand
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.UserRole
import com.example.decorra.util.isValidEmail

@Composable
fun AuthScreen(
    isRegister: Boolean,
    role: UserRole,
    onRoleChange: (UserRole) -> Unit,
    onSuccess: () -> Unit,
    onSwitchMode: () -> Unit
) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    var firstError by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastError by rememberSaveable { mutableStateOf<Int?>(null) }
    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<Int?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(if (isRegister) R.string.create_account_title else R.string.welcome),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        RoleToggle(role, onRoleChange)
        Spacer(Modifier.height(20.dp))

        if (isRegister) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DecorraTextField(
                    label = stringResource(R.string.first_name),
                    value = first,
                    onValueChange = { first = it; firstError = null },
                    modifier = Modifier.weight(1f),
                    error = firstError
                )
                DecorraTextField(
                    label = stringResource(R.string.last_name),
                    value = last,
                    onValueChange = { last = it; lastError = null },
                    modifier = Modifier.weight(1f),
                    error = lastError
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        DecorraTextField(
            label = stringResource(R.string.email_address),
            value = email,
            onValueChange = { email = it; emailError = null },
            placeholder = stringResource(R.string.email_hint),
            keyboardType = KeyboardType.Email,
            error = emailError
        )
        Spacer(Modifier.height(8.dp))
        DecorraTextField(
            label = stringResource(R.string.password),
            value = password,
            onValueChange = { password = it; passwordError = null },
            isPassword = true,
            error = passwordError
        )

        if (isRegister) {
            Spacer(Modifier.height(8.dp))
            DecorraTextField(
                label = stringResource(R.string.confirm_password),
                value = confirm,
                onValueChange = { confirm = it; confirmError = null },
                isPassword = true,
                error = confirmError
            )
            Spacer(Modifier.height(16.dp))
        } else {
            TextButton(
                onClick = { /* reset flow added with data storage */ },
                modifier = Modifier.align(Alignment.End)
            ) { Text(stringResource(R.string.forgot_password)) }
        }

        PrimaryButton(
            text = stringResource(if (isRegister) R.string.create_account else R.string.sign_in),
            onClick = {
                emailError = when {
                    email.isBlank() -> R.string.error_required
                    !isValidEmail(email) -> R.string.error_email_invalid
                    else -> null
                }
                if (isRegister) {
                    firstError = if (first.isBlank()) R.string.error_required else null
                    lastError = if (last.isBlank()) R.string.error_required else null
                    passwordError = when {
                        password.isBlank() -> R.string.error_required
                        password.length < 8 -> R.string.error_password_short
                        else -> null
                    }
                    confirmError = when {
                        confirm.isBlank() -> R.string.error_required
                        confirm != password -> R.string.error_password_mismatch
                        else -> null
                    }
                } else {
                    passwordError = if (password.isBlank()) R.string.error_required else null
                }
                val ok = listOf(firstError, lastError, emailError, passwordError, confirmError)
                    .all { it == null }
                if (ok) onSuccess()
            }
        )
        Spacer(Modifier.height(8.dp))
        SecondaryButton(
            text = stringResource(if (isRegister) R.string.have_account else R.string.create_account),
            onClick = onSwitchMode
        )
    }
}

@Composable
private fun RoleToggle(role: UserRole, onRoleChange: (UserRole) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Taupe.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        listOf(
            UserRole.CLIENT to R.string.role_client,
            UserRole.DECORATOR to R.string.role_decorator
        ).forEach { (value, label) ->
            val selected = value == role
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Sand else Color.Transparent)
                    .selectable(selected = selected, onClick = { onRoleChange(value) }, role = Role.Tab)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(label),
                    color = Espresso,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}