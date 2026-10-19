package com.example.decorra.ui.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.decorra.R
import com.example.decorra.navigation.Routes
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.EmptyState

@StringRes
private fun title(key: String): Int = when (key) {
    Routes.ADMIN_BUSINESS -> R.string.menu_business
    Routes.ADMIN_TEAM -> R.string.menu_team
    Routes.ADMIN_BANK -> R.string.menu_payments
    else -> R.string.section_empty_title
}

@Composable
fun AdminSectionScreen(sectionKey: String, onBack: () -> Unit) {
    when (sectionKey) {
        Routes.ADMIN_TEAM -> TeamScreen(onBack)
        Routes.ADMIN_BUSINESS -> BusinessProfileScreen(onBack)
        Routes.ADMIN_BANK -> BankDetailsScreen(onBack)
        else -> Column(Modifier.fillMaxSize()) {
            DecorraTopBar(
                title = stringResource(title(sectionKey)),
                navigationIcon = { BackButton(onBack) }
            )
            EmptyState(
                title = R.string.section_empty_title,
                message = R.string.section_empty_message
            )
        }
    }
}