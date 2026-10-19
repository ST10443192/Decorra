package com.example.decorra.ui.client

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Pricing
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.data.QuoteWorkflow
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.InfoRow
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.ui.components.labelRes
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.dateText
import com.example.decorra.util.moneyText
import com.example.decorra.util.shortageMessage

@Composable
fun QuoteDetailScreen(quoteId: String, onBack: () -> Unit) {
    val quotes by QuoteRepository.quotes.collectAsState()
    val quote = quotes.firstOrNull { it.id == quoteId }
    val context = LocalContext.current
    var showDecline by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = quote?.themeName.orEmpty(),
            navigationIcon = { BackButton(onBack) }
        )

        if (quote == null) {
            EmptyState(title = R.string.quote_not_found)
            return@Column
        }

        val deposit = Pricing.deposit(quote.totalCents)
        val depositPaid = quote.status.ordinal >= QuoteStatus.DEPOSIT_PAID.ordinal
        val remaining = if (depositPaid) quote.totalCents - deposit else quote.totalCents
        val approvedToast = stringResource(R.string.quote_approved_toast)
        val declinedToast = stringResource(R.string.quote_declined_toast)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(title = null) { StatusStepper(quote.status) }

            if (quote.status == QuoteStatus.SUBMITTED && quote.sentToClient) {
                InfoCard(R.string.review_quote_heading) {
                    Text(
                        stringResource(R.string.review_quote_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    PrimaryButton(
                        text = stringResource(R.string.approve_quote),
                        onClick = {
                            val blocked = QuoteWorkflow.changeStatus(
                                quote, QuoteStatus.APPROVED, byClient = true
                            )
                            val message = if (blocked.isEmpty()) approvedToast
                            else context.shortageMessage(blocked)
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                    SecondaryButton(
                        text = stringResource(R.string.decline_quote),
                        onClick = { showDecline = true }
                    )
                }
            }

            InfoCard(R.string.event_details) {
                if (quote.eventType.isNotBlank()) {
                    InfoRow(stringResource(R.string.event_type_label), quote.eventType)
                }
                InfoRow(stringResource(R.string.label_date), dateText(quote.eventEpochDay))
                if (quote.venue.isNotBlank()) {
                    InfoRow(stringResource(R.string.venue_label), quote.venue)
                }
                InfoRow(stringResource(R.string.label_guests), quote.guests.toString())
            }

            InfoCard(title = null) {
                InfoRow(stringResource(R.string.total_estimate), moneyText(quote.totalCents), bold = true)
                InfoRow(
                    stringResource(R.string.deposit_label, Pricing.DEPOSIT_PERCENT),
                    moneyText(deposit)
                )
                InfoRow(stringResource(R.string.remaining_balance), moneyText(remaining))
            }

            if (quote.status.ordinal >= QuoteStatus.APPROVED.ordinal) PaymentDetailsCard()

            InfoCard(R.string.items_heading) {
                quote.lines.forEach { line ->
                    InfoRow(
                        stringResource(R.string.item_line, line.name, line.quantity),
                        moneyText(line.totalCents)
                    )
                }
            }

            InfoCard(R.string.tasks_heading) {
                if (quote.tasks.isEmpty()) {
                    Text(
                        stringResource(R.string.tasks_empty),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    quote.tasks.forEach { task ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = task.done,
                                onCheckedChange = { QuoteRepository.toggleTask(quote.id, task.id) }
                            )
                            Text(task.title, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Plain-text summary for the phone's share sheet.
            val shareLabel = stringResource(R.string.share_quote)
            val header = stringResource(R.string.quote_share_title, quote.themeName)
            val dateLine = "${stringResource(R.string.label_date)}: ${dateText(quote.eventEpochDay)}"
            val guestsLine = "${stringResource(R.string.label_guests)}: ${quote.guests}"
            val itemLines = quote.lines.map {
                "${stringResource(R.string.item_line, it.name, it.quantity)}: ${moneyText(it.totalCents)}"
            }
            val totalLine = "${stringResource(R.string.total_estimate)}: ${moneyText(quote.totalCents)}"
            val shareText = (listOf(header, dateLine, guestsLine) + itemLines + totalLine)
                .joinToString("\n")

            SecondaryButton(
                text = shareLabel,
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(send, shareLabel))
                }
            )

            Spacer(Modifier.height(8.dp))
        }

        if (showDecline) {
            AlertDialog(
                onDismissRequest = { showDecline = false },
                title = { Text(stringResource(R.string.decline_confirm_title)) },
                text = { Text(stringResource(R.string.decline_confirm_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        QuoteWorkflow.changeStatus(quote, QuoteStatus.REJECTED, byClient = true)
                        Toast.makeText(context, declinedToast, Toast.LENGTH_SHORT).show()
                        showDecline = false
                    }) { Text(stringResource(R.string.decline_quote)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDecline = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun StatusStepper(current: QuoteStatus) {
    if (current == QuoteStatus.REJECTED) {
        Text(
            stringResource(R.string.quote_rejected_message),
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    Row(Modifier.fillMaxWidth()) {
        QuoteStatus.entries.filter { it != QuoteStatus.REJECTED }.forEach { step ->
            val reached = step.ordinal <= current.ordinal
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(if (reached) Espresso else Taupe.copy(alpha = 0.4f), CircleShape)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(step.labelRes()),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}