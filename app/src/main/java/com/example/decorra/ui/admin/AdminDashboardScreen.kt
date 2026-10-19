package com.example.decorra.ui.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Inventory
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.ChatRepository
import com.example.decorra.data.NotificationRepository
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.data.StockLevel
import com.example.decorra.data.UserSession
import com.example.decorra.data.stockLevel
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.StatusChip
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.dateText
import java.time.LocalDate
import com.example.decorra.data.NotificationPreferences
import com.example.decorra.data.allows
import com.example.decorra.data.category

@Composable
fun AdminDashboardScreen(
    onRequests: () -> Unit,
    onEvents: () -> Unit,
    onMessages: () -> Unit,
    onQuote: (String) -> Unit,
    onAlerts: () -> Unit,
    onInventory: () -> Unit
) {
    val quotes by QuoteRepository.quotes.collectAsState()
    val threads by remember { ChatRepository.threads() }
        .collectAsState(initial = emptyList<com.example.decorra.data.ChatThread>())
    val items by ItemRepository.items.collectAsState()
    val notifications by NotificationRepository.items.collectAsState()
    val today = remember { LocalDate.now().toEpochDay() }

    val pending = quotes.count { it.status == QuoteStatus.SUBMITTED }
    val booked = quotes.count { it.status in Inventory.RESERVING }
    val approved = quotes.count { it.status.ordinal >= QuoteStatus.APPROVED.ordinal }
    val rejected = quotes.count { it.status == QuoteStatus.REJECTED }
    val decided = approved + rejected
    val approvalRate = if (decided == 0) null else approved * 100 / decided
    val awaitingReply = threads.count { it.lastFromClient }

    val upcoming = quotes
        .filter { it.status in Inventory.RESERVING && it.eventEpochDay in today..(today + 14) }
        .sortedBy { it.eventEpochDay }
    val lowStock = items.filter {
        it.stockLevel() == StockLevel.LOW || it.stockLevel() == StockLevel.CRITICAL
    }
    val prefs by NotificationPreferences.state.collectAsState()
    val unread = notifications.count {
        it.forAdmin && !it.read && prefs.allows(UserSession.email, it.category())
    }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.app_name),
            actions = {
                IconButton(onClick = onAlerts) {
                    BadgedBox(badge = { if (unread > 0) Badge { Text(unread.toString()) } }) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = if (unread > 0) {
                                stringResource(R.string.alerts_button_unread, unread)
                            } else {
                                stringResource(R.string.alerts_button)
                            }
                        )
                    }
                }
            }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val name = UserSession.firstName
            Text(
                text = if (name.isBlank()) stringResource(R.string.admin_greeting_blank)
                else stringResource(R.string.admin_greeting, name),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                stringResource(R.string.admin_dashboard_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(R.string.tile_pending_quotations, pending.toString(), onRequests, Modifier.weight(1f))
                StatTile(R.string.tile_active_bookings, booked.toString(), onEvents, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    R.string.tile_approval_rate,
                    approvalRate?.let { stringResource(R.string.percent_format, it) }
                        ?: stringResource(R.string.approval_rate_none),
                    onRequests,
                    Modifier.weight(1f)
                )
                StatTile(R.string.tile_awaiting_reply, awaitingReply.toString(), onMessages, Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            InfoCard(R.string.low_stock_alerts_heading) {
                if (lowStock.isEmpty()) {
                    Text(
                        stringResource(R.string.stock_all_healthy),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    lowStock.take(3).forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, modifier = Modifier.weight(1f))
                            Text(stringResource(item.stockLevel().labelRes()))
                        }
                    }
                }
                TextButton(onClick = onInventory) { Text(stringResource(R.string.manage)) }
            }

            Spacer(Modifier.height(12.dp))
            InfoCard(R.string.upcoming_tasks_heading) {
                Text(
                    stringResource(R.string.upcoming_14_days),
                    style = MaterialTheme.typography.bodySmall
                )
                if (upcoming.isEmpty()) {
                    Text(
                        stringResource(R.string.upcoming_none),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    upcoming.forEach { quote ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onQuote(quote.id) }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(quote.themeName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${quote.clientLabel()}, ${dateText(quote.eventEpochDay)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            StatusChip(quote.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    @StringRes label: Int,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Taupe.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge)
        }
    }
}