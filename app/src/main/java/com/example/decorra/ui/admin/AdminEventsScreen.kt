package com.example.decorra.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Quote
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.QuoteStatus
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.StatusChip
import com.example.decorra.ui.theme.Cream
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Sage
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.dateText
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import com.example.decorra.data.Inventory

@Composable
fun AdminEventsScreen(onEventClick: (String) -> Unit) {
    val quotes by QuoteRepository.quotes.collectAsState()
    val events = remember(quotes) {
        quotes
            .filter { it.status in Inventory.RESERVING || it.status == QuoteStatus.COMPLETE }
            .sortedBy { it.eventEpochDay }
    }
    val eventDays = remember(events) { events.map { it.eventEpochDay }.toSet() }
    val today = remember { LocalDate.now().toEpochDay() }

    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var selectedDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val month = YearMonth.parse(monthText)

    val chosenDay = selectedDay
    val shown = if (chosenDay != null) events.filter { it.eventEpochDay == chosenDay }
    else events.filter { it.eventEpochDay >= today }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(title = stringResource(R.string.events_title))

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MonthCalendar(
                    month = month,
                    selectedDay = chosenDay,
                    today = today,
                    eventDays = eventDays,
                    onPrevious = { monthText = month.minusMonths(1).toString() },
                    onNext = { monthText = month.plusMonths(1).toString() },
                    onDayClick = { day -> selectedDay = if (selectedDay == day) null else day }
                )
            }

            item {
                Text(
                    text = if (chosenDay != null) stringResource(R.string.events_on_day, dateText(chosenDay))
                    else stringResource(R.string.events_upcoming),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (shown.isEmpty()) {
                item {
                    Text(
                        text = stringResource(
                            if (chosenDay != null) R.string.events_day_empty
                            else R.string.events_empty_message
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(shown, key = { it.id }) { quote ->
                    EventCard(quote) { onEventClick(quote.id) }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    selectedDay: Long?,
    today: Long,
    eventDays: Set<Long>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDayClick: (Long) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val weekStart = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val offset = (month.atDay(1).dayOfWeek.value - weekStart.value + 7) % 7
    val length = month.lengthOfMonth()
    val rows = (offset + length + 6) / 7

    InfoCard(title = null) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.calendar_previous)
                )
            }
            Text(
                text = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.calendar_next)
                )
            }
        }

        Row(Modifier.fillMaxWidth()) {
            repeat(7) { index ->
                Text(
                    text = weekStart.plus(index.toLong()).getDisplayName(TextStyle.NARROW, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val day = row * 7 + col - offset + 1
                    Box(Modifier.weight(1f)) {
                        if (day in 1..length) {
                            val epochDay = month.atDay(day).toEpochDay()
                            DayCell(
                                day = day,
                                selected = epochDay == selectedDay,
                                isToday = epochDay == today,
                                hasEvent = epochDay in eventDays,
                                onClick = { onDayClick(epochDay) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    selected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(if (selected) Espresso else Color.Transparent)
            .then(
                if (isToday && !selected) Modifier.border(1.dp, Taupe, shape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.toString(),
            color = if (selected) Cream else Espresso,
            style = MaterialTheme.typography.bodyMedium
        )
        Box(
            Modifier
                .size(6.dp)
                .background(
                    when {
                        !hasEvent -> Color.Transparent
                        selected -> Cream
                        else -> Espresso
                    },
                    CircleShape
                )
        )
    }
}

@Composable
private fun EventCard(quote: Quote, onClick: () -> Unit) {
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
            Text(
                "${quote.clientLabel()}, ${dateText(quote.eventEpochDay)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (quote.venue.isNotBlank()) {
                Text(quote.venue, style = MaterialTheme.typography.bodyMedium)
            }
            if (quote.tasks.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { quote.progress() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Sage,
                    trackColor = Taupe.copy(alpha = 0.3f)
                )
                Text(
                    stringResource(
                        R.string.tasks_progress,
                        quote.tasks.count { it.done },
                        quote.tasks.size
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}