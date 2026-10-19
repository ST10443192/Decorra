package com.example.decorra.util

import android.content.Intent

enum class UserRole {
    CLIENT, DECORATOR;

    companion object {
        const val EXTRA = "extra_role"

        fun fromIntent(intent: Intent): UserRole =
            intent.getStringExtra(EXTRA)?.let { runCatching { valueOf(it) }.getOrNull() } ?: CLIENT
    }
}