package com.example.decorra.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.decorra.R
import java.text.NumberFormat

/** Formats cents using the number style of the selected app language. */
@Composable
fun moneyText(cents: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    val number = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(cents / 100.0)
    return stringResource(R.string.money_format, number)
}