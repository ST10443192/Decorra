package com.example.decorra.ui.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
//import androidx.compose.ui.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.DecorTheme
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.QuoteDraftRepository
import com.example.decorra.data.ThemeRepository
import com.example.decorra.data.lines
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorImage
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.util.moneyText

@Composable
fun PortfolioScreen(
    onBack: () -> Unit,
    onThemeClick: (String) -> Unit,
    onRentContinue: () -> Unit
) {
    val themes by ThemeRepository.themes.collectAsState()
    val catalog by ItemRepository.items.collectAsState()
    val draft by QuoteDraftRepository.draft.collectAsState()

    var tab by rememberSaveable { mutableStateOf(0) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    // Opening the rent tab starts a fresh rental draft (it keeps one that is already running).
    LaunchedEffect(tab) {
        if (tab == 1) QuoteDraftRepository.start(QuoteDraftRepository.RENTAL)
    }

    val visibleThemes = remember(themes, query) {
        if (query.isBlank()) themes
        else themes.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    val allRentable = remember(catalog) { catalog.filter { it.rentable } }
    val visibleRentable = remember(allRentable, query) {
        if (query.isBlank()) allRentable
        else allRentable.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    val isRental = draft.themeId == QuoteDraftRepository.RENTAL
    val rentalQuantities = if (isRental) draft.quantities else emptyMap()
    val rentalLines = if (isRental) draft.lines(catalog) else emptyList()
    val subtotal = rentalLines.sumOf { it.totalCents }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.portfolio_title),
            navigationIcon = { BackButton(onBack) },
            actions = {
                IconButton(onClick = {
                    searching = !searching
                    if (!searching) query = ""
                }) {
                    Icon(
                        imageVector = if (searching) Icons.Outlined.Close else Icons.Outlined.Search,
                        contentDescription = stringResource(
                            if (searching) R.string.close_search else R.string.search
                        )
                    )
                }
            }
        )

        if (searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(stringResource(if (tab == 0) R.string.search_hint else R.string.search_rent_hint))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        TabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Espresso
        ) {
            listOf(R.string.browse_events_tab, R.string.browse_rent_tab).forEachIndexed { index, label ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(stringResource(label)) }
                )
            }
        }

        androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
            if (tab == 0) {
                when {
                    themes.isEmpty() -> EmptyState(
                        title = R.string.portfolio_empty_title,
                        message = R.string.portfolio_empty_message
                    )
                    visibleThemes.isEmpty() -> EmptyState(
                        title = R.string.no_results_title,
                        message = R.string.no_results_message
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        gridItems(visibleThemes, key = { it.id }) { theme ->
                            ThemeCard(theme) { onThemeClick(theme.id) }
                        }
                    }
                }
            } else {
                when {
                    allRentable.isEmpty() -> EmptyState(
                        title = R.string.rent_empty_title,
                        message = R.string.rent_empty_message
                    )
                    visibleRentable.isEmpty() -> EmptyState(
                        title = R.string.no_results_title,
                        message = R.string.no_results_message
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleRentable, key = { it.id }) { item ->
                            ItemRow(
                                item = item,
                                quantity = rentalQuantities[item.id] ?: 0,
                                onQuantity = { QuoteDraftRepository.setQuantity(item.id, it) }
                            )
                        }
                    }
                }
            }
        }

        if (tab == 1 && allRentable.isNotEmpty()) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.current_estimate))
                    Text(
                        moneyText(subtotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = stringResource(R.string.continue_label),
                    onClick = onRentContinue,
                    enabled = rentalLines.isNotEmpty()
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(theme: DecorTheme, onClick: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)) {
        DecorImage(theme.imageUri, null, Modifier.fillMaxWidth().aspectRatio(1f))
        Spacer(Modifier.height(6.dp))
        Text(theme.name, style = MaterialTheme.typography.labelLarge)
    }
}