package com.example.decorra.util

import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.example.decorra.R
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.text(): String = editText?.text?.toString().orEmpty()

fun TextInputLayout.clearErrorOnEdit() {
    editText?.doAfterTextChanged { error = null }
}

private fun TextInputLayout.fail(@StringRes res: Int): Boolean {
    error = context.getString(res)
    return false
}

fun TextInputLayout.requireText(): Boolean =
    if (text().isBlank()) fail(R.string.error_required) else true

fun TextInputLayout.requireEmail(): Boolean = when {
    text().isBlank() -> fail(R.string.error_required)
    !isValidEmail(text()) -> fail(R.string.error_email_invalid)
    else -> true
}

fun TextInputLayout.requireNewPassword(): Boolean = when {
    text().isBlank() -> fail(R.string.error_required)
    text().length < 8 -> fail(R.string.error_password_short)
    else -> true
}

fun TextInputLayout.requireMatch(other: TextInputLayout): Boolean = when {
    text().isBlank() -> fail(R.string.error_required)
    text() != other.text() -> fail(R.string.error_password_mismatch)
    else -> true
}

fun TextInputLayout.requirePhone(): Boolean {
    val digits = text().count { it.isDigit() }
    return when {
        text().isBlank() -> fail(R.string.error_required)
        digits !in 9..15 -> fail(R.string.error_phone_invalid)
        else -> true
    }
}