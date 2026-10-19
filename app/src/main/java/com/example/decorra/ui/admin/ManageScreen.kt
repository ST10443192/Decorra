package com.example.decorra.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.DecorTheme
import com.example.decorra.data.ImageStore
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.ThemeRepository
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorImage
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.ImagePickerField
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.theme.Espresso
import com.example.decorra.util.moneyText
import java.util.UUID

@Composable
fun ManageScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val themes by ThemeRepository.themes.collectAsState()
    val catalog by ItemRepository.items.collectAsState()

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.manage_title),
            navigationIcon = { BackButton(onBack) }
        )

        TabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Espresso
        ) {
            listOf(R.string.tab_themes, R.string.tab_items).forEachIndexed { index, label ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(stringResource(label)) }
                )
            }
        }

        Box(Modifier.weight(1f)) {
            if (tab == 0) {
                if (themes.isEmpty()) {
                    EmptyState(title = R.string.themes_manage_empty)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(themes, key = { it.id }) { theme ->
                            ManageRow(
                                title = theme.name,
                                subtitle = theme.description,
                                imagePath = theme.imageUri,
                                deleteLabel = stringResource(R.string.delete_theme, theme.name),
                                onDelete = { ThemeRepository.remove(theme.id) }
                            )
                        }
                    }
                }
            } else {
                if (catalog.isEmpty()) {
                    EmptyState(title = R.string.items_manage_empty)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(catalog, key = { it.id }) { item ->
                            val themeName = themes.firstOrNull { it.id == item.themeId }?.name
                                ?: stringResource(R.string.all_themes)
                            val price = stringResource(R.string.price_each, moneyText(item.priceCents))
                            val rentableTag = stringResource(R.string.rentable_tag)
                            ManageRow(
                                title = item.name,
                                subtitle = listOfNotNull(
                                    price,
                                    themeName,
                                    rentableTag.takeIf { item.rentable }
                                ).joinToString(", "),
                                imagePath = item.imageUri,
                                deleteLabel = stringResource(R.string.delete_item, item.name),
                                onDelete = { ItemRepository.remove(item.id) },
                                onClick = { editingId = item.id }
                            )
                        }
                    }
                }
            }
        }

        PrimaryButton(
            text = stringResource(if (tab == 0) R.string.add_theme else R.string.add_item),
            onClick = { showDialog = true },
            modifier = Modifier.padding(20.dp)
        )
    }

    if (showDialog) {
        if (tab == 0) {
            AddThemeDialog(
                onDismiss = { showDialog = false },
                onSave = {
                    ThemeRepository.add(it)
                    showDialog = false
                }
            )
        } else {
            ItemDialog(
                initial = null,
                themes = themes,
                onDismiss = { showDialog = false },
                onSave = {
                    ItemRepository.add(it)
                    showDialog = false
                }
            )
        }
    }

    val editing = catalog.firstOrNull { it.id == editingId }
    if (editing != null) {
        ItemDialog(
            initial = editing,
            themes = themes,
            onDismiss = { editingId = null },
            onSave = {
                ItemRepository.update(it)
                editingId = null
            }
        )
    }
}

@Composable
private fun ManageRow(
    title: String,
    subtitle: String,
    imagePath: String?,
    deleteLabel: String,
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DecorImage(imagePath, null, Modifier.size(56.dp), maxSidePx = 200)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = deleteLabel)
        }
    }
}

@Composable
private fun AddThemeDialog(onDismiss: () -> Unit, onSave: (DecorTheme) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var image by rememberSaveable { mutableStateOf<String?>(null) }
    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var descriptionError by rememberSaveable { mutableStateOf<Int?>(null) }

    // A photo picked but never saved must not stay on the phone.
    val discard = {
        ImageStore.delete(image)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = discard,
        title = { Text(stringResource(R.string.add_theme)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImagePickerField(imagePath = image, onImagePicked = { image = it })
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.theme_name)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descriptionError = null },
                    label = { Text(stringResource(R.string.theme_description)) },
                    minLines = 2,
                    maxLines = 4,
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(stringResource(it)) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text(stringResource(R.string.theme_tags)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameError = if (name.isBlank()) R.string.error_required else null
                descriptionError = if (description.isBlank()) R.string.error_required else null
                if (nameError == null && descriptionError == null) {
                    onSave(
                        DecorTheme(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            description = description.trim(),
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            imageUri = image
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