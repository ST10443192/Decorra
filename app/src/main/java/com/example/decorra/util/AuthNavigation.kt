package com.example.decorra.util

import android.app.Activity
import android.content.Intent
import com.example.decorra.MainActivity
import com.example.decorra.ui.auth.SplashActivity

fun Activity.openApp(role: UserRole) {
    startActivity(
        Intent(this, MainActivity::class.java)
            .putExtra(UserRole.EXTRA, role.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}

fun Activity.openAuth(target: Class<out Activity>, role: UserRole) {
    startActivity(Intent(this, target).putExtra(UserRole.EXTRA, role.name))
}

fun Activity.restartAtSplash() {
    startActivity(
        Intent(this, SplashActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}