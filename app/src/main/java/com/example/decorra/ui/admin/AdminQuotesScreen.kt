package com.example.decorra.ui.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.StatusChip
import com.example.decorra.ui.theme.Cream
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.dateText
import com.example.decorra.util.moneyText

private enum class QuoteFilter(@StringRes val label: Int) {
    ALL(R.string.filter_all),
    PENDING(R.string.filter_pending),
    APPROVED(R.string.filter_approved),
    REJECTED(R.string.filter_rejected);

    fun matches(quote: Quote): Boolean = when (this) {
        ALL -> true
        PENDING -> quote.status == QuoteStatus.SUBMITTED
        APPROVED -> quote.status.ordinal >= QuoteStatus.APPROVED.ordinal
        REJECTED -> quote.status == QuoteStatus.REJECTED
    }
}

@Composable
fun AdminQuotesScreen(onQuoteClick: (String) -> Unit) {
    val allQuotes by QuoteRepository.quotes.collectAsState()
    // A client's draft is their private work in progress, so the decorator does not see it.
    val quotes = allQuotes.filter { it.status != QuoteStatus.DRAFT }

    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val filter = QuoteFilter.entries[selectedIndex]
    val visible = quotes.filter { filter.matches(it) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(title = stringResource(R.string.admin_quotes_title))

        if (quotes.isEmpty()) {
            EmptyState(
                title = R.string.admin_quotes_empty_title,
                message = R.string.admin_quotes_empty_message
            )
            return@Column
        }

        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuoteFilter.entries.forEachIndexed { index, option ->
                val count = quotes.count { option.matches(it) }
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    label = {
                        Text(
                            stringResource(
                                R.string.filter_with_count,
                                stringResource(option.label),
                                count
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Espresso,
                        selectedLabelColor = Cream
                    )
                )
            }
        }

        if (visible.isEmpty()) {
            Box(Modifier.weight(1f)) {
                EmptyState(title = R.string.admin_quotes_filter_empty)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible.asReversed(), key = { it.id }) { quote ->
                    AdminQuoteCard(quote) { onQuoteClick(quote.id) }
                }
            }
        }
    }
}

@Composable
private fun AdminQuoteCard(quote: Quote, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Taupe.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    quote.clientLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(quote.status)
            }
            Text(
                "${quote.themeName}, ${dateText(quote.eventEpochDay)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.estimate_label))
                Text(moneyText(quote.totalCents), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}