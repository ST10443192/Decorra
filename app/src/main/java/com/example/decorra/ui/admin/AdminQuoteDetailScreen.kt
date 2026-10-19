package com.example.decorra.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.DecorItem
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.Pricing
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.data.QuoteWorkflow
import com.example.decorra.ui.client.ItemRow
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.InfoRow
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.ui.components.labelRes
import com.example.decorra.ui.theme.Cream
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.util.dateText
import com.example.decorra.util.moneyText
import com.example.decorra.util.shortageMessage

private val tabTitles = listOf(
    R.string.tab_details,
    R.string.tab_builder,
    R.string.tab_summary,
    R.string.tab_notes
)

@Composable
fun AdminQuoteDetailScreen(quoteId: String, onBack: () -> Unit) {
    val quotes by QuoteRepository.quotes.collectAsState()
    val quote = quotes.firstOrNull { it.id == quoteId }
    var tab by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = quote?.let { stringResource(R.string.quote_for, it.clientLabel()) }.orEmpty(),
            navigationIcon = { BackButton(onBack) }
        )

        if (quote == null) {
            EmptyState(title = R.string.quote_not_found)
            return@Column
        }

        TabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Espresso
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(stringResource(title), maxLines = 1) }
                )
            }
        }

        Column(Modifier.weight(1f)) {
            when (tab) {
                0 -> DetailsTab(quote)
                1 -> BuilderTab(quote)
                2 -> SummaryTab(quote, onBack)
                else -> NotesTab(quote)
            }
        }
    }
}

@Composable
private fun TabColumn(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { content() }
}

@Composable
private fun ClientCard(quote: Quote) {
    InfoCard(R.string.client_heading) {
        Text(quote.clientLabel(), style = MaterialTheme.typography.titleSmall)
        if (quote.clientEmail.isNotBlank()) {
            Text(quote.clientEmail, style = MaterialTheme.typography.bodyMedium)
        }
        if (quote.clientPhone.isNotBlank()) {
            Text(quote.clientPhone, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EventCard(quote: Quote) {
    InfoCard(R.string.event_details) {
        InfoRow(stringResource(R.string.label_theme), quote.themeName)
        if (quote.eventType.isNotBlank()) {
            InfoRow(stringResource(R.string.event_type_label), quote.eventType)
        }
        InfoRow(stringResource(R.string.label_date), dateText(quote.eventEpochDay))
        if (quote.venue.isNotBlank()) {
            InfoRow(stringResource(R.string.venue_label), quote.venue)
        }
        InfoRow(stringResource(R.string.label_guests), quote.guests.toString())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsTab(quote: Quote) {
    val context = LocalContext.current

    TabColumn {
        ClientCard(quote)
        EventCard(quote)
        InfoCard(R.string.status_heading) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuoteStatus.entries.filter { it != QuoteStatus.DRAFT }.forEach { status ->
                    FilterChip(
                        selected = quote.status == status,
                        onClick = {
                            val blocked = QuoteWorkflow.changeStatus(quote, status, byClient = false)
                            if (blocked.isNotEmpty()) {
                                Toast.makeText(
                                    context,
                                    context.shortageMessage(blocked),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        label = { Text(stringResource(status.labelRes())) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Espresso,
                            selectedLabelColor = Cream
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BuilderTab(quote: Quote) {
    val catalog by ItemRepository.items.collectAsState()

    // Every catalog item, plus any quote line whose item is no longer in the catalog.
    val extra = quote.lines
        .filter { line -> catalog.none { it.id == line.itemId } }
        .map { DecorItem(it.itemId, it.name, it.unitPriceCents) }
    val rows = catalog + extra

    if (rows.isEmpty()) {
        EmptyState(
            title = R.string.builder_empty_title,
            message = R.string.builder_empty_message
        )
        return
    }

    TabColumn {
        rows.forEach { item ->
            ItemRow(
                item = item,
                quantity = quote.lines.firstOrNull { it.itemId == item.id }?.quantity ?: 0,
                onQuantity = { QuoteRepository.setQuantity(quote.id, item, it) }
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.current_estimate))
            Text(moneyText(quote.subtotalCents), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SummaryTab(quote: Quote, onBack: () -> Unit) {
    val context = LocalContext.current
    val savedMessage = stringResource(R.string.changes_saved)
    val sentMessage = stringResource(R.string.quote_sent)

    TabColumn {
        ClientCard(quote)
        EventCard(quote)
        InfoCard(R.string.items_heading) {
            quote.lines.forEach { line ->
                InfoRow(
                    stringResource(R.string.item_line, line.name, line.quantity),
                    moneyText(line.totalCents)
                )
            }
        }
        InfoCard(title = null) {
            InfoRow(stringResource(R.string.subtotal), moneyText(quote.subtotalCents))
            InfoRow(
                stringResource(R.string.vat_label, Pricing.VAT_PERCENT),
                moneyText(quote.vatCents)
            )
            InfoRow(stringResource(R.string.total_estimate), moneyText(quote.totalCents), bold = true)
            InfoRow(
                stringResource(R.string.deposit_label, Pricing.DEPOSIT_PERCENT),
                moneyText(Pricing.deposit(quote.totalCents))
            )
        }

        SecondaryButton(
            text = stringResource(R.string.save_draft),
            onClick = {
                Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                onBack()
            }
        )
        PrimaryButton(
            text = stringResource(R.string.send_to_client),
            onClick = {
                QuoteWorkflow.sendToClient(quote)
                Toast.makeText(context, sentMessage, Toast.LENGTH_SHORT).show()
                onBack()
            }
        )
    }
}

@Composable
private fun NotesTab(quote: Quote) = TabColumn {
    var text by rememberSaveable(quote.id) { mutableStateOf(quote.note) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            QuoteRepository.setNote(quote.id, it)
        },
        modifier = Modifier.fillMaxWidth().height(220.dp),
        placeholder = { Text(stringResource(R.string.notes_hint)) },
        shape = RoundedCornerShape(12.dp)
    )
}