package com.example.decorra.ui.admin

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.QuoteRepository
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.InfoRow
import com.example.decorra.ui.components.StatusChip
import com.example.decorra.ui.theme.Sage
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.dateText
import kotlin.math.roundToInt

@Composable
fun AdminEventDetailScreen(quoteId: String, onBack: () -> Unit) {
    val quotes by QuoteRepository.quotes.collectAsState()
    val quote = quotes.firstOrNull { it.id == quoteId }
    var newTask by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = quote?.themeName.orEmpty(),
            navigationIcon = { BackButton(onBack) }
        )

        if (quote == null) {
            EmptyState(title = R.string.quote_not_found)
            return@Column
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(title = null) {
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
                if (quote.eventType.isNotBlank()) {
                    InfoRow(stringResource(R.string.event_type_label), quote.eventType)
                }
                InfoRow(stringResource(R.string.label_date), dateText(quote.eventEpochDay))
                if (quote.venue.isNotBlank()) {
                    InfoRow(stringResource(R.string.venue_label), quote.venue)
                }
                InfoRow(stringResource(R.string.label_guests), quote.guests.toString())
                if (quote.clientPhone.isNotBlank()) {
                    Text(quote.clientPhone, style = MaterialTheme.typography.bodyMedium)
                }
            }

            InfoCard(R.string.setup_progress) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(
                            R.string.tasks_progress,
                            quote.tasks.count { it.done },
                            quote.tasks.size
                        )
                    )
                    Text(
                        stringResource(R.string.percent_format, (quote.progress() * 100).roundToInt()),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { quote.progress() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Sage,
                    trackColor = Taupe.copy(alpha = 0.3f)
                )
            }

            InfoCard(R.string.tasks_heading) {
                if (quote.tasks.isEmpty()) {
                    Text(
                        stringResource(R.string.admin_tasks_empty),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                quote.tasks.forEach { task ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { QuoteRepository.toggleTask(quote.id, task.id) }
                        )
                        Text(task.title, modifier = Modifier.weight(1f))
                        IconButton(onClick = { QuoteRepository.removeTask(quote.id, task.id) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete_task, task.title)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.new_task_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.padding(4.dp))
                    Button(
                        onClick = {
                            val title = newTask.trim()
                            if (title.isNotEmpty()) {
                                QuoteRepository.addTask(quote.id, title)
                                newTask = ""
                            }
                        },
                        enabled = newTask.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(stringResource(R.string.add_task)) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}