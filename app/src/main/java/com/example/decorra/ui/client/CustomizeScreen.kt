package com.example.decorra.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.DecorItem
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.QuoteDraftRepository
import com.example.decorra.data.ThemeRepository
import com.example.decorra.data.lines
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorImage
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.util.moneyText

@Composable
fun CustomizeScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val draft by QuoteDraftRepository.draft.collectAsState()
    val catalog by ItemRepository.items.collectAsState()
    val themes by ThemeRepository.themes.collectAsState()
    var showAll by rememberSaveable { mutableStateOf(false) }

    val themeName = themes.firstOrNull { it.id == draft.themeId }?.name.orEmpty()

    // Items made for this theme, or for every theme.
    val themeItems = catalog.filter { it.themeId == null || it.themeId == draft.themeId }
    // Items from elsewhere that the decorator allows to be rented on their own.
    val extras = catalog.filter { item -> item.rentable && themeItems.none { it.id == item.id } }

    val showExtras = showAll || themeItems.isEmpty()
    val visible = if (showExtras) themeItems + extras else themeItems

    val lines = draft.lines(catalog)
    val subtotal = lines.sumOf { it.totalCents }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(title = themeName, navigationIcon = { BackButton(onBack) })

        if (themeItems.isEmpty() && extras.isEmpty()) {
            EmptyState(
                title = R.string.items_empty_title,
                message = R.string.items_empty_message
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { item ->
                    ItemRow(
                        item = item,
                        quantity = draft.quantities[item.id] ?: 0,
                        onQuantity = { QuoteDraftRepository.setQuantity(item.id, it) }
                    )
                }
                if (!showExtras && extras.isNotEmpty()) {
                    item {
                        TextButton(onClick = { showAll = true }) {
                            Text(stringResource(R.string.add_more_items))
                        }
                    }
                }
            }

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
                Spacer(Modifier.size(12.dp))
                PrimaryButton(
                    text = stringResource(R.string.continue_label),
                    onClick = onContinue,
                    enabled = lines.isNotEmpty()
                )
            }
        }
    }
}

@Composable
fun ItemRow(item: DecorItem, quantity: Int, onQuantity: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        DecorImage(item.imageUri, null, Modifier.size(56.dp), maxSidePx = 200)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.price_each, moneyText(item.priceCents)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = { onQuantity(quantity - 1) }, enabled = quantity > 0) {
            Icon(
                Icons.Outlined.Remove,
                contentDescription = stringResource(R.string.decrease_quantity, item.name)
            )
        }
        Text(
            quantity.toString(),
            modifier = Modifier.widthIn(min = 28.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { onQuantity(quantity + 1) }) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = stringResource(R.string.increase_quantity, item.name)
            )
        }
    }
}