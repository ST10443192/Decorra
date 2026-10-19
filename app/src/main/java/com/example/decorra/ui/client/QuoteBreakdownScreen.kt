package com.example.decorra.ui.client

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Inventory
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.Pricing
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteDraftRepository
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.data.QuoteWorkflow
import com.example.decorra.data.ThemeRepository
import com.example.decorra.data.UserSession
import com.example.decorra.data.lines
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.InfoRow
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.ui.components.ShortageCard
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.moneyText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun QuoteBreakdownScreen(onBack: () -> Unit, onDone: () -> Unit) {
    val draft by QuoteDraftRepository.draft.collectAsState()
    val catalog by ItemRepository.items.collectAsState()
    val themes by ThemeRepository.themes.collectAsState()
    val allQuotes by QuoteRepository.quotes.collectAsState()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val quoteId = remember { UUID.randomUUID().toString() }

    val submittedMessage = stringResource(R.string.quote_submitted)
    val savedMessage = stringResource(R.string.draft_saved)
    val rentalName = stringResource(R.string.individual_rental)

    val themeId = draft.themeId
    val themeName = if (themeId == QuoteDraftRepository.RENTAL) rentalName
    else themes.firstOrNull { it.id == themeId }?.name
    val lines = draft.lines(catalog)
    val day = draft.eventEpochDay
    val guests = draft.guests

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.quote_breakdown_title),
            navigationIcon = { BackButton(onBack) }
        )

        if (themeId == null || themeName == null || day == null || guests == null || lines.isEmpty()) {
            EmptyState(title = R.string.theme_not_found)
            return@Column
        }

        val quote = Quote(
            id = quoteId,
            themeId = themeId,
            themeName = themeName,
            eventEpochDay = day,
            guests = guests,
            lines = lines,
            status = QuoteStatus.DRAFT,
            clientName = UserSession.fullName,
            clientEmail = UserSession.email,
            clientPhone = UserSession.phone,
            eventType = draft.eventType,
            venue = draft.venue
        )
        val shortages = Inventory.shortages(quote, catalog, allQuotes)

        fun finish(submit: Boolean, message: String) {
            if (submit) QuoteWorkflow.submit(quote)
            else QuoteRepository.add(quote.copy(status = QuoteStatus.DRAFT))
            QuoteDraftRepository.clear()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onDone()
        }

        val dateText = LocalDate.ofEpochDay(day)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShortageCard(shortages)

            InfoCard(R.string.event_details) {
                InfoRow(stringResource(R.string.label_theme), themeName)
                if (quote.eventType.isNotBlank()) {
                    InfoRow(stringResource(R.string.event_type_label), quote.eventType)
                }
                InfoRow(stringResource(R.string.label_date), dateText)
                if (quote.venue.isNotBlank()) {
                    InfoRow(stringResource(R.string.venue_label), quote.venue)
                }
                InfoRow(stringResource(R.string.label_guests), guests.toString())
            }
            InfoCard(R.string.items_heading) {
                lines.forEach { line ->
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
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Taupe.copy(alpha = 0.5f))
                InfoRow(
                    stringResource(R.string.total_estimate),
                    moneyText(quote.totalCents),
                    bold = true
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                text = stringResource(R.string.submit_quote_request),
                onClick = { finish(submit = true, message = submittedMessage) },
                enabled = shortages.isEmpty()
            )
            SecondaryButton(
                text = stringResource(R.string.save_as_draft),
                onClick = { finish(submit = false, message = savedMessage) }
            )
        }
    }
}