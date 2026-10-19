package com.example.decorra.ui.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.DecorItem
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.StockLevel
import com.example.decorra.data.stockLevel
import com.example.decorra.data.totalValueCents
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorImage
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.theme.Cream
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.ui.theme.Sage
import com.example.decorra.ui.theme.Sand
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.util.moneyText

@StringRes
fun StockLevel.labelRes(): Int = when (this) {
    StockLevel.NOT_TRACKED -> R.string.stock_status_untracked
    StockLevel.HEALTHY -> R.string.stock_status_ok
    StockLevel.LOW -> R.string.stock_status_low
    StockLevel.CRITICAL -> R.string.stock_status_critical
}

@Composable
fun InventoryScreen(onBack: () -> Unit) {
    val items by ItemRepository.items.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var restockId by rememberSaveable { mutableStateOf<String?>(null) }

    val visible = remember(items, query) {
        if (query.isBlank()) items
        else items.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.inventory_title),
            navigationIcon = { BackButton(onBack) }
        )

        if (items.isEmpty()) {
            EmptyState(
                title = R.string.inventory_empty_title,
                message = R.string.inventory_empty_message
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryTile(
                            R.string.inv_total_value,
                            moneyText(items.sumOf { it.totalValueCents }),
                            Modifier.weight(1f)
                        )
                        SummaryTile(
                            R.string.inv_total_items,
                            items.size.toString(),
                            Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryTile(
                            R.string.inv_low_stock,
                            items.count { it.stockLevel() == StockLevel.LOW }.toString(),
                            Modifier.weight(1f)
                        )
                        SummaryTile(
                            R.string.inv_critical_stock,
                            items.count { it.stockLevel() == StockLevel.CRITICAL }.toString(),
                            Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.inv_search)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                items(visible, key = { it.id }) { item ->
                    InventoryRow(item) { restockId = item.id }
                }
            }
        }
    }

    val target = items.firstOrNull { it.id == restockId }
    if (target != null) {
        RestockDialog(target) { restockId = null }
    }
}

@Composable
private fun SummaryTile(@StringRes label: Int, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Taupe.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun LevelChip(level: StockLevel) {
    val (background, content) = when (level) {
        StockLevel.HEALTHY -> Sage.copy(alpha = 0.6f) to Espresso
        StockLevel.LOW -> Sand to Espresso
        StockLevel.CRITICAL -> Espresso to Cream
        StockLevel.NOT_TRACKED -> Taupe.copy(alpha = 0.4f) to Espresso
    }
    Text(
        text = stringResource(level.labelRes()),
        color = content,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun InventoryRow(item: DecorItem, onRestock: () -> Unit) {
    InfoCard(title = null) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DecorImage(item.imageUri, null, Modifier.size(48.dp), maxSidePx = 200)
            Spacer(Modifier.width(12.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            LevelChip(item.stockLevel())
        }
        val stock = item.stock
        Text(
            text = if (stock == null) stringResource(R.string.stock_untracked_line)
            else stringResource(R.string.stock_line, stock, item.minStock),
            style = MaterialTheme.typography.bodyMedium
        )
        if (item.unitCostCents > 0) {
            Text(
                stringResource(
                    R.string.unit_cost_line,
                    moneyText(item.unitCostCents),
                    moneyText(item.totalValueCents)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onRestock) { Text(stringResource(R.string.restock)) }
        }
    }
}

@Composable
private fun RestockDialog(item: DecorItem, onDismiss: () -> Unit) {
    var quantity by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.restock_title, item.name)) },
        text = {
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    if (it.length <= 6 && it.all(Char::isDigit)) {
                        quantity = it
                        error = null
                    }
                },
                label = { Text(stringResource(R.string.restock_quantity)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = error != null,
                supportingText = error?.let { { Text(stringResource(it)) } },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = quantity.toIntOrNull()
                if (amount == null || amount <= 0) {
                    error = R.string.error_quantity_invalid
                } else {
                    ItemRepository.restock(item.id, amount)
                    onDismiss()
                }
            }) { Text(stringResource(R.string.restock)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}