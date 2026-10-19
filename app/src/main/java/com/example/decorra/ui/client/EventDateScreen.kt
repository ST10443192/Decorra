package com.example.decorra.ui.client

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Inventory
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.QuoteDraftRepository
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.lines
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.ShortageCard
import com.example.decorra.ui.theme.Cream
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Taupe
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
private fun FormField(
    @StringRes label: Int,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
    @StringRes error: Int? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDateScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val draft by QuoteDraftRepository.draft.collectAsState()
    val catalog by ItemRepository.items.collectAsState()
    val quotes by QuoteRepository.quotes.collectAsState()

    val todayUtc = remember {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = draft.eventEpochDay?.let {
            LocalDate.ofEpochDay(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
        }
    )

    var guests by rememberSaveable { mutableStateOf(draft.guests?.toString().orEmpty()) }
    var eventType by rememberSaveable { mutableStateOf(draft.eventType) }
    var venue by rememberSaveable { mutableStateOf(draft.venue) }
    var dateError by rememberSaveable { mutableStateOf<Int?>(null) }
    var guestsError by rememberSaveable { mutableStateOf<Int?>(null) }
    var typeError by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectedDay = pickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
    }
    val shortages = if (selectedDay == null) emptyList()
    else Inventory.shortages(draft.lines(catalog), selectedDay, catalog, quotes)

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.event_date_title),
            navigationIcon = { BackButton(onBack) }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            DatePicker(
                state = pickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background,
                    selectedDayContainerColor = Espresso,
                    selectedDayContentColor = Cream,
                    todayDateBorderColor = Taupe,
                    todayContentColor = Espresso
                )
            )
            dateError?.let {
                Text(
                    stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            ShortageCard(shortages, Modifier.padding(top = 12.dp))

            Spacer(Modifier.height(16.dp))
            FormField(
                label = R.string.guests_label,
                value = guests,
                onChange = {
                    if (it.length <= 5 && it.all(Char::isDigit)) {
                        guests = it
                        guestsError = null
                    }
                },
                keyboardType = KeyboardType.Number,
                error = guestsError
            )
            Spacer(Modifier.height(8.dp))
            FormField(
                label = R.string.event_type_label,
                value = eventType,
                onChange = { eventType = it; typeError = null },
                placeholder = stringResource(R.string.event_type_hint),
                error = typeError
            )
            Spacer(Modifier.height(8.dp))
            FormField(
                label = R.string.venue_field,
                value = venue,
                onChange = { venue = it }
            )
            Spacer(Modifier.height(16.dp))
        }

        PrimaryButton(
            text = stringResource(R.string.continue_label),
            onClick = {
                val count = guests.toIntOrNull()
                dateError = if (selectedDay == null) R.string.error_date_required else null
                guestsError = if (count == null || count < 1) R.string.error_guests_invalid else null
                typeError = if (eventType.isBlank()) R.string.error_required else null
                if (selectedDay != null && count != null && count >= 1 && typeError == null) {
                    QuoteDraftRepository.setEvent(selectedDay, count, eventType.trim(), venue.trim())
                    onContinue()
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}