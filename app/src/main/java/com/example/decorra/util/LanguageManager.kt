package com.example.decorra.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {
    const val SYSTEM = "system"

    fun set(tag: String) {
        val locales = if (tag == SYSTEM) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun selectedTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty()) SYSTEM else locales[0]?.language ?: SYSTEM
    }
}