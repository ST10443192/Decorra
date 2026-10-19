package com.example.decorra.ui.client

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.UserSession
import com.example.decorra.navigation.Routes
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.ui.theme.Taupe

@Composable
fun ProfileScreen(
    onSection: (String) -> Unit,
    onNotifications: () -> Unit,
    onSettings: () -> Unit,
    onLogOut: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.profile_title),
            actions = {
                IconButton(onClick = onSettings) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings_title)
                    )
                }
            }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Taupe
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    if (UserSession.fullName.isNotBlank()) {
                        Text(UserSession.fullName, style = MaterialTheme.typography.titleMedium)
                    }
                    if (UserSession.email.isNotBlank()) {
                        Text(UserSession.email, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (UserSession.phone.isNotBlank()) {
                        Text(UserSession.phone, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            ProfileRow(R.string.profile_my_events, Icons.Outlined.Event) {
                onSection(Routes.SECTION_EVENTS)
            }
            ProfileRow(R.string.profile_payment_history, Icons.Outlined.Payments) {
                onSection(Routes.SECTION_PAYMENTS)
            }
            ProfileRow(R.string.profile_saved_quotes, Icons.Outlined.BookmarkBorder) {
                onSection(Routes.SECTION_SAVED)
            }
            ProfileRow(R.string.profile_my_inspiration, Icons.Outlined.Lightbulb) {
                onSection(Routes.SECTION_INSPIRATION)
            }
            ProfileRow(R.string.notifications, Icons.Outlined.Notifications, onNotifications)

            Spacer(Modifier.padding(4.dp))
            SecondaryButton(stringResource(R.string.log_out), onLogOut)
        }
    }
}

@Composable
private fun ProfileRow(@StringRes label: Int, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Taupe.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Text(stringResource(label), modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}