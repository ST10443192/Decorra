package com.example.decorra.ui.client

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Pricing
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.data.UserSession
import com.example.decorra.navigation.Routes
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.InfoRow
import com.example.decorra.util.dateText
import com.example.decorra.util.moneyText

@StringRes
private fun sectionTitle(key: String): Int = when (key) {
    Routes.SECTION_EVENTS -> R.string.profile_my_events
    Routes.SECTION_PAYMENTS -> R.string.profile_payment_history
    Routes.SECTION_SAVED -> R.string.profile_saved_quotes
    Routes.SECTION_INSPIRATION -> R.string.profile_my_inspiration
    Routes.SECTION_DESIGNS -> R.string.tile_my_designs
    else -> R.string.section_empty_title
}

private fun Quote.isPaidOrLater() = status.ordinal >= QuoteStatus.DEPOSIT_PAID.ordinal

@Composable
fun SectionScreen(sectionKey: String, onBack: () -> Unit, onQuoteClick: (String) -> Unit) {
    // My inspiration has its own full screen with its own top bar.
    if (sectionKey == Routes.SECTION_INSPIRATION) {
        InspirationScreen(onBack)
        return
    }

    val all by QuoteRepository.quotes.collectAsState()
    val mine = all.filter { it.clientEmail.equals(UserSession.email, ignoreCase = true) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(sectionTitle(sectionKey)),
            navigationIcon = { BackButton(onBack) }
        )

        when (sectionKey) {
            Routes.SECTION_EVENTS -> QuoteListSection(
                quotes = mine.filter { it.isPaidOrLater() }.sortedBy { it.eventEpochDay },
                emptyTitle = R.string.client_events_empty,
                onQuoteClick = onQuoteClick
            )
            Routes.SECTION_SAVED -> QuoteListSection(
                quotes = mine.filter { it.status == QuoteStatus.DRAFT },
                emptyTitle = R.string.saved_drafts_empty,
                onQuoteClick = onQuoteClick
            )
            Routes.SECTION_DESIGNS -> QuoteListSection(
                quotes = mine.asReversed(),
                emptyTitle = R.string.quotes_empty_title,
                onQuoteClick = onQuoteClick
            )
            Routes.SECTION_PAYMENTS -> PaymentsSection(mine.filter { it.isPaidOrLater() })
            else -> EmptyState(
                title = R.string.section_empty_title,
                message = R.string.section_empty_message
            )
        }
    }
}

@Composable
private fun QuoteListSection(
    quotes: List<Quote>,
    @StringRes emptyTitle: Int,
    onQuoteClick: (String) -> Unit
) {
    if (quotes.isEmpty()) {
        EmptyState(title = emptyTitle)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quotes, key = { it.id }) { quote ->
                QuoteCard(quote) { onQuoteClick(quote.id) }
            }
        }
    }
}

@Composable
private fun PaymentsSection(quotes: List<Quote>) {
    if (quotes.isEmpty()) {
        EmptyState(title = R.string.payments_empty, message = R.string.payments_note)
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.payments_note), style = MaterialTheme.typography.bodyMedium)
        }
        items(quotes, key = { it.id }) { quote ->
            val deposit = Pricing.deposit(quote.totalCents)
            InfoCard(title = null) {
                Text(quote.themeName, style = MaterialTheme.typography.titleSmall)
                Text(dateText(quote.eventEpochDay), style = MaterialTheme.typography.bodyMedium)
                InfoRow(
                    stringResource(R.string.deposit_label, Pricing.DEPOSIT_PERCENT),
                    moneyText(deposit)
                )
                if (quote.status == QuoteStatus.COMPLETE) {
                    InfoRow(
                        stringResource(R.string.remaining_balance),
                        moneyText(quote.totalCents - deposit)
                    )
                }
            }
        }
    }
}