package com.example.decorra.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.decorra.R
import com.example.decorra.data.Shortage

/** Lists what is short on the chosen date. Shows nothing when everything is available. */
@Composable
fun ShortageCard(shortages: List<Shortage>, modifier: Modifier = Modifier) {
    if (shortages.isEmpty()) return

    InfoCard(R.string.shortage_title, modifier) {
        shortages.forEach { shortage ->
            Text(
                stringResource(
                    R.string.shortage_line,
                    shortage.itemName,
                    shortage.requested,
                    shortage.available
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(stringResource(R.string.shortage_hint), style = MaterialTheme.typography.bodySmall)
    }
}