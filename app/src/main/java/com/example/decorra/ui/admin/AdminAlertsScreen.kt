package com.example.decorra.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.AppNotification
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.NotificationRepository
import com.example.decorra.data.StockLevel
import com.example.decorra.data.stockLevel
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.displayBody
import com.example.decorra.ui.components.displayTitle
import com.example.decorra.util.timeText

@Composable
fun AdminAlertsScreen(onBack: () -> Unit, onInventory: () -> Unit) {
    val all by NotificationRepository.items.collectAsState()
    val items by ItemRepository.items.collectAsState()

    val alerts = all.filter { it.forAdmin }.sortedByDescending { it.timeMillis }
    val lowStock = items.filter {
        it.stockLevel() == StockLevel.LOW || it.stockLevel() == StockLevel.CRITICAL
    }
    val hasUnread = alerts.any { !it.read }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.alerts_title),
            navigationIcon = { BackButton(onBack) },
            actions = {
                if (hasUnread) {
                    TextButton(onClick = { NotificationRepository.markAdminRead() }) {
                        Text(stringResource(R.string.mark_all_read))
                    }
                }
            }
        )

        if (alerts.isEmpty() && lowStock.isEmpty()) {
            EmptyState(
                title = R.string.alerts_empty_title,
                message = R.string.alerts_empty_message
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (lowStock.isNotEmpty()) {
                    item {
                        InfoCard(R.string.low_stock_alerts_heading) {
                            lowStock.forEach { item ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.name, modifier = Modifier.weight(1f))
                                    Text(stringResource(item.stockLevel().labelRes()))
                                }
                            }
                            TextButton(onClick = onInventory) {
                                Text(stringResource(R.string.manage))
                            }
                        }
                    }
                }
                items(alerts, key = { it.id }) { AlertCard(it) }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AppNotification) {
    InfoCard(title = null) {
        Text(
            alert.displayTitle(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (alert.read) FontWeight.Normal else FontWeight.Bold
        )
        Text(alert.displayBody(), style = MaterialTheme.typography.bodyMedium)
        Text(timeText(alert.timeMillis), style = MaterialTheme.typography.labelSmall)
    }
}