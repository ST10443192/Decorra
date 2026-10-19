package com.example.decorra.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.DecorItem
import com.example.decorra.data.DecorTheme
import com.example.decorra.data.ImageStore
import com.example.decorra.ui.components.ImagePickerField
import com.example.decorra.ui.theme.Cream
import com.example.decorra.ui.theme.Espresso
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID

private fun priceText(cents: Long): String =
    if (cents % 100 == 0L) (cents / 100).toString()
    else String.format(Locale.US, "%d.%02d", cents / 100, cents % 100)

private fun parseCents(text: String): Long? =
    text.replace(',', '.').toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))
        ?.setScale(0, RoundingMode.HALF_UP)
        ?.toLong()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDialog(
    initial: DecorItem?,
    themes: List<DecorTheme>,
    onDismiss: () -> Unit,
    onSave: (DecorItem) -> Unit
) {
    val original = initial?.imageUri

    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(initial?.let { priceText(it.priceCents) }.orEmpty()) }
    var themeId by rememberSaveable { mutableStateOf(initial?.themeId) }
    var image by rememberSaveable { mutableStateOf(original) }
    var rentable by rememberSaveable { mutableStateOf(initial?.rentable ?: false) }
    var stock by rememberSaveable { mutableStateOf(initial?.stock?.toString().orEmpty()) }
    var minStock by rememberSaveable {
        mutableStateOf(initial?.minStock?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var unitCost by rememberSaveable {
        mutableStateOf(initial?.unitCostCents?.takeIf { it > 0 }?.let { priceText(it) }.orEmpty())
    }

    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var priceError by rememberSaveable { mutableStateOf<Int?>(null) }
    var stockError by rememberSaveable { mutableStateOf<Int?>(null) }
    var minError by rememberSaveable { mutableStateOf<Int?>(null) }
    var costError by rememberSaveable { mutableStateOf<Int?>(null) }

    // A photo picked but never saved must not stay on the phone; the original one is kept.
    val discard = {
        if (image != original) ImageStore.delete(image)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = discard,
        title = {
            Text(stringResource(if (initial == null) R.string.add_item else R.string.edit_item))
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImagePickerField(
                    imagePath = image,
                    onImagePicked = { image = it },
                    keepPath = original
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.item_name)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                            price = it
                            priceError = null
                        }
                    },
                    label = { Text(stringResource(R.string.item_price)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = {
                        if (it.length <= 6 && it.all(Char::isDigit)) {
                            stock = it
                            stockError = null
                        }
                    },
                    label = { Text(stringResource(R.string.item_stock)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = stockError != null,
                    supportingText = stockError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = {
                        if (it.length <= 6 && it.all(Char::isDigit)) {
                            minStock = it
                            minError = null
                        }
                    },
                    label = { Text(stringResource(R.string.item_min_stock)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = minError != null,
                    supportingText = minError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unitCost,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                            unitCost = it
                            costError = null
                        }
                    },
                    label = { Text(stringResource(R.string.item_unit_cost)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = costError != null,
                    supportingText = costError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    stringResource(R.string.item_available_for),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeId == null,
                        onClick = { themeId = null },
                        label = { Text(stringResource(R.string.all_themes)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Espresso,
                            selectedLabelColor = Cream
                        )
                    )
                    themes.forEach { theme ->
                        FilterChip(
                            selected = themeId == theme.id,
                            onClick = { themeId = theme.id },
                            label = { Text(theme.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Espresso,
                                selectedLabelColor = Cream
                            )
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = rentable,
                            role = Role.Switch,
                            onValueChange = { rentable = it }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.rent_individually_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            stringResource(R.string.rent_individually_helper),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = rentable, onCheckedChange = null)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = parseCents(price)
                val stockValue = if (stock.isBlank()) null else stock.toIntOrNull()
                val minValue = if (minStock.isBlank()) 0 else minStock.toIntOrNull()
                val costCents = if (unitCost.isBlank()) 0L else parseCents(unitCost)

                nameError = if (name.isBlank()) R.string.error_required else null
                priceError = if (cents == null || cents <= 0) R.string.error_price_invalid else null
                stockError = if (stock.isNotBlank() && stockValue == null) R.string.error_number_invalid else null
                minError = if (minValue == null) R.string.error_number_invalid else null
                costError = if (costCents == null) R.string.error_price_invalid else null

                val valid = nameError == null && priceError == null && stockError == null &&
                        minError == null && costError == null
                if (valid && cents != null && minValue != null && costCents != null) {
                    if (image != original) ImageStore.delete(original)
                    onSave(
                        DecorItem(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            priceCents = cents,
                            themeId = themeId,
                            imageUri = image,
                            rentable = rentable,
                            stock = stockValue,
                            minStock = minValue,
                            unitCostCents = costCents
                        )
                    )
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = discard) { Text(stringResource(R.string.cancel)) }
        }
    )
}