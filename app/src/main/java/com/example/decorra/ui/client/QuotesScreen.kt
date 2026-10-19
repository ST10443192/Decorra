package com.example.decorra.ui.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.StatusChip
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.dateText
import com.example.decorra.util.moneyText
import com.example.decorra.data.UserSession

@Composable
fun QuotesScreen(onQuoteClick: (String) -> Unit, onBrowsePortfolio: () -> Unit) {
    val allQuotes by QuoteRepository.quotes.collectAsState()
    val quotes = allQuotes.filter { it.clientEmail.equals(UserSession.email, ignoreCase = true) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(title = stringResource(R.string.quotes_title))

        if (quotes.isEmpty()) {
            EmptyState(
                title = R.string.quotes_empty_title,
                message = R.string.quotes_empty_message,
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.browse_portfolio),
                        onClick = onBrowsePortfolio
                    )
                }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quotes.asReversed(), key = { it.id }) { quote ->
                    QuoteCard(quote) { onQuoteClick(quote.id) }
                }
            }
        }
    }
}

@Composable
fun QuoteCard(quote: Quote, onClick: () -> Unit) {
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
                    quote.themeName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(quote.status)
            }
            Text(dateText(quote.eventEpochDay), style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.total_estimate))
                Text(moneyText(quote.totalCents), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}