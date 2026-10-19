package com.example.decorra.ui.client

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.UserSession
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.ImagePlaceholder
import com.example.decorra.ui.theme.Sand
import com.example.decorra.ui.theme.Taupe

@Composable
fun HomeScreen(
    onBrowsePortfolio: () -> Unit,
    onQuotes: () -> Unit,
    onMyDesigns: () -> Unit,
    onMessageDecorator: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.app_name),
            navigationIcon = {
                IconButton(onClick = onProfile) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = stringResource(R.string.nav_profile)
                    )
                }
            },
            actions = {
                IconButton(onClick = onNotifications) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.notifications)
                    )
                }
            }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val firstName = UserSession.firstName
            Text(
                text = if (firstName.isBlank()) stringResource(R.string.home_welcome)
                else stringResource(R.string.home_welcome_name, firstName),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Sand.copy(alpha = 0.35f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.home_banner_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.home_banner_body),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onBrowsePortfolio, shape = RoundedCornerShape(10.dp)) {
                            Text(stringResource(R.string.browse_portfolio))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    ImagePlaceholder(Modifier.size(96.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeTile(R.string.tile_quotes, Icons.Outlined.Description, onQuotes, Modifier.weight(1f))
                HomeTile(R.string.tile_my_designs, Icons.Outlined.Palette, onMyDesigns, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeTile(
                    R.string.tile_message_decorator,
                    Icons.Outlined.ChatBubbleOutline,
                    onMessageDecorator,
                    Modifier.weight(1f)
                )
                HomeTile(R.string.tile_profile, Icons.Outlined.PersonOutline, onProfile, Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            BusinessInfoCard()
        }
    }
}

@Composable
private fun HomeTile(
    @StringRes label: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Taupe.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(label),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}