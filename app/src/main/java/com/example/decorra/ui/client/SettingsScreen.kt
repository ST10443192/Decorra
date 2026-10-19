package com.example.decorra.ui.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.MenuRow

private enum class SettingsPage { HUB, PROFILE, PASSWORD, NOTIFICATIONS, LANGUAGE }

/** Settings for both clients and decorators. Sub-pages are swapped in place. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.HUB.name) }
    val current = SettingsPage.valueOf(page)
    val toHub = { page = SettingsPage.HUB.name }

    BackHandler(enabled = current != SettingsPage.HUB) { toHub() }

    when (current) {
        SettingsPage.HUB -> SettingsHub(onBack) { page = it.name }
        SettingsPage.PROFILE -> EditProfilePage(toHub)
        SettingsPage.PASSWORD -> ChangePasswordPage(toHub)
        SettingsPage.NOTIFICATIONS -> NotificationPreferencesPage(toHub)
        SettingsPage.LANGUAGE -> LanguagePage(toHub)
    }
}

@Composable
private fun SettingsHub(onBack: () -> Unit, onOpen: (SettingsPage) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.settings_title),
            navigationIcon = { BackButton(onBack) }
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuRow(R.string.settings_edit_profile, Icons.Outlined.PersonOutline) {
                onOpen(SettingsPage.PROFILE)
            }
            MenuRow(R.string.settings_change_password, Icons.Outlined.Lock) {
                onOpen(SettingsPage.PASSWORD)
            }
            MenuRow(R.string.settings_notifications, Icons.Outlined.Notifications) {
                onOpen(SettingsPage.NOTIFICATIONS)
            }
            MenuRow(R.string.language_heading, Icons.Outlined.Language) {
                onOpen(SettingsPage.LANGUAGE)
            }
        }
    }
}