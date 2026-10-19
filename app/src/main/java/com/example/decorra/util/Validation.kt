package com.example.decorra.util

import android.util.Patterns

fun isValidEmail(value: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()