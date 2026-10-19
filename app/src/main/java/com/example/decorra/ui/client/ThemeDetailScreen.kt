package com.example.decorra.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.ThemeRepository
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.theme.Taupe
import com.example.decorra.ui.components.DecorImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeDetailScreen(
    themeId: String,
    onBack: () -> Unit,
    onCustomize: (String) -> Unit
) {
    val themes by ThemeRepository.themes.collectAsState()
    val theme = themes.firstOrNull { it.id == themeId }
    var saved by rememberSaveable(themeId) { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = theme?.name.orEmpty(),
            navigationIcon = { BackButton(onBack) },
            actions = {
                if (theme != null) {
                    IconButton(onClick = { saved = !saved }) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(
                                if (saved) R.string.remove_favourite else R.string.add_favourite
                            )
                        )
                    }
                }
            }
        )

        if (theme == null) {
            EmptyState(title = R.string.theme_not_found)
        } else {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                DecorImage(
                    path = theme.imageUri,
                    contentDescription = theme.name,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    maxSidePx = 1280
                )
                Spacer(Modifier.height(16.dp))
                Text(theme.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(theme.description, style = MaterialTheme.typography.bodyLarge)

                if (theme.tags.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        theme.tags.forEach { tag ->
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .background(Taupe.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            PrimaryButton(
                text = stringResource(R.string.customize_theme),
                onClick = { onCustomize(theme.id) },
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}