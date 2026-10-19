package com.example.decorra.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.QuoteStatus
import com.example.decorra.ui.theme.Sand

@StringRes
fun QuoteStatus.labelRes(): Int = when (this) {
    QuoteStatus.DRAFT -> R.string.status_draft
    QuoteStatus.SUBMITTED -> R.string.status_submitted
    QuoteStatus.REJECTED -> R.string.status_rejected
    QuoteStatus.APPROVED -> R.string.status_approved
    QuoteStatus.DEPOSIT_PAID -> R.string.status_deposit_paid
    QuoteStatus.IN_PROGRESS -> R.string.status_in_progress
    QuoteStatus.COMPLETE -> R.string.status_complete
}

@Composable
fun StatusChip(status: QuoteStatus, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(status.labelRes()),
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(Sand.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}