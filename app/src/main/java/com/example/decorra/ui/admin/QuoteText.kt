package com.example.decorra.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.decorra.R
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository

@Composable
fun Quote.clientLabel(): String = clientName.ifBlank { stringResource(R.string.client_unknown) }

/** Share of tasks done, from 0 to 1. */
fun Quote.progress(): Float =
    if (tasks.isEmpty()) 0f else tasks.count { it.done } / tasks.size.toFloat()

/** Until Step 8 there is one shared conversation, named after the latest client who requested a quote. */
@Composable
fun clientThreadName(): String {
    val quotes by QuoteRepository.quotes.collectAsState()
    return quotes.lastOrNull { it.clientName.isNotBlank() }?.clientName
        ?: stringResource(R.string.client_unknown)
}