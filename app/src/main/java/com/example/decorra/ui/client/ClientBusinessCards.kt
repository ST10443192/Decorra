package com.example.decorra.ui.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.decorra.R
import com.example.decorra.data.BusinessRepository
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.InfoRow

/** Where to pay the deposit. Shows nothing until the decorator has entered bank details. */
@Composable
fun PaymentDetailsCard() {
    val bank by BusinessRepository.bank.collectAsState()
    if (!bank.isComplete) return

    InfoCard(R.string.payment_details_heading) {
        InfoRow(stringResource(R.string.bank_name), bank.bankName)
        InfoRow(stringResource(R.string.account_holder), bank.accountHolder)
        InfoRow(stringResource(R.string.account_number), bank.accountNumber)
        if (bank.branchCode.isNotBlank()) {
            InfoRow(stringResource(R.string.branch_code), bank.branchCode)
        }
        if (bank.reference.isNotBlank()) {
            Text(bank.reference, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** The decorator's business card at the bottom of the client Home screen. */
@Composable
fun BusinessInfoCard() {
    val profile by BusinessRepository.profile.collectAsState()
    if (profile.isEmpty) return

    InfoCard(R.string.about_decorator_heading) {
        if (profile.name.isNotBlank()) {
            Text(profile.name, style = MaterialTheme.typography.titleSmall)
        }
        if (profile.phone.isNotBlank()) {
            Text(profile.phone, style = MaterialTheme.typography.bodyMedium)
        }
        if (profile.email.isNotBlank()) {
            Text(profile.email, style = MaterialTheme.typography.bodyMedium)
        }
        if (profile.address.isNotBlank()) {
            Text(profile.address, style = MaterialTheme.typography.bodyMedium)
        }
        if (profile.about.isNotBlank()) {
            Text(profile.about, style = MaterialTheme.typography.bodyMedium)
        }
    }
}