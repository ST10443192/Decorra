package com.example.decorra.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.Account
import com.example.decorra.data.AccountRepository
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.InfoCard

@Composable
fun TeamScreen(onBack: () -> Unit) {
    val team by remember { AccountRepository.observeTeam() }
        .collectAsState(initial = emptyList<Account>())

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.menu_team),
            navigationIcon = { BackButton(onBack) }
        )

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.team_members_heading),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(team, key = { it.id }) { account ->
                InfoCard(title = null) {
                    Text(
                        "${account.firstName} ${account.lastName}".trim(),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(account.email, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}