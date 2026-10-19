package com.example.decorra.ui.client

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.AppNotification
import com.example.decorra.data.Inventory
import com.example.decorra.data.NotificationCategory
import com.example.decorra.data.NotificationPreferences
import com.example.decorra.data.NotificationRepository
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.UserSession
import com.example.decorra.data.allows
import com.example.decorra.data.category
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.displayBody
import com.example.decorra.ui.components.displayTitle
import com.example.decorra.util.timeText
import java.time.LocalDate

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val all by NotificationRepository.items.collectAsState()
    val quotes by QuoteRepository.quotes.collectAsState()
    val prefs by NotificationPreferences.state.collectAsState()
    val today = remember { LocalDate.now().toEpochDay() }
    val email = UserSession.email

    val items = all
        .filter {
            !it.forAdmin &&
                    it.clientEmail.equals(email, ignoreCase = true) &&
                    prefs.allows(email, it.category())
        }
        .sortedByDescending { it.timeMillis }

    val countdowns = if (!prefs.allows(email, NotificationCategory.COUNTDOWN)) {
        emptyList()
    } else {
        quotes
            .filter {
                it.clientEmail.equals(email, ignoreCase = true) &&
                        it.status in Inventory.RESERVING &&
                        it.eventEpochDay >= today
            }
            .sortedBy { it.eventEpochDay }
    }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.notifications),
            navigationIcon = { BackButton(onBack) }
        )

        if (items.isEmpty() && countdowns.isEmpty()) {
            EmptyState(
                title = R.string.notifications_empty_title,
                message = R.string.notifications_empty_message
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(countdowns, key = { "countdown-${it.id}" }) { CountdownCard(it, today) }
                items(items, key = { it.id }) { NotificationCard(it) }
            }
        }
    }
}

@Composable
private fun CountdownCard(quote: Quote, today: Long) {
    val days = (quote.eventEpochDay - today).toInt()
    val text = if (days <= 0) stringResource(R.string.event_countdown_today)
    else LocalContext.current.resources.getQuantityString(R.plurals.event_countdown_days, days, days)

    InfoCard(R.string.notif_countdown_title) {
        Text(
            stringResource(R.string.countdown_line, quote.themeName, text),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NotificationCard(item: AppNotification) {
    InfoCard(title = null) {
        Text(item.displayTitle(), style = MaterialTheme.typography.titleSmall)
        Text(item.displayBody(), style = MaterialTheme.typography.bodyMedium)
        Text(timeText(item.timeMillis), style = MaterialTheme.typography.labelSmall)
    }
}