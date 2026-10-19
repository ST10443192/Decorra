package com.example.decorra.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.UserSession
import com.example.decorra.navigation.Routes
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.MenuRow
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.ui.theme.Taupe

@Composable
fun AdminProfileScreen(
    onManage: () -> Unit,
    onInventory: () -> Unit,
    onSection: (String) -> Unit,
    onSettings: () -> Unit,
    onLogOut: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(title = stringResource(R.string.more_title))

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
                }
            }

            MenuRow(R.string.menu_business, Icons.Outlined.Business) {
                onSection(Routes.ADMIN_BUSINESS)
            }
            MenuRow(R.string.menu_team, Icons.Outlined.Groups) {
                onSection(Routes.ADMIN_TEAM)
            }
            MenuRow(R.string.menu_services, Icons.Outlined.Inventory2, onManage)
            MenuRow(R.string.menu_inventory, Icons.Outlined.Inventory, onInventory)
            MenuRow(R.string.menu_payments, Icons.Outlined.Payments) {
                onSection(Routes.ADMIN_BANK)
            }
            MenuRow(R.string.menu_app_settings, Icons.Outlined.Settings, onSettings)

            Spacer(Modifier.padding(4.dp))
            SecondaryButton(stringResource(R.string.log_out), onLogOut)
        }
    }
}