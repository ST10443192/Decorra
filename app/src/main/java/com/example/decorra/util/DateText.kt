package com.example.decorra.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

@Composable
fun dateText(epochDay: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return LocalDate.ofEpochDay(epochDay)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
}

@Composable
fun timeText(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(millis))
}