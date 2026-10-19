package com.example.decorra.navigation

import androidx.compose.runtime.Composable
import com.example.decorra.util.UserRole

@Composable
fun AppNavHost(role: UserRole, onLogOut: () -> Unit) {
    when (role) {
        UserRole.CLIENT -> ClientNavHost(onLogOut)
        UserRole.DECORATOR -> AdminNavHost(onLogOut)
    }
}