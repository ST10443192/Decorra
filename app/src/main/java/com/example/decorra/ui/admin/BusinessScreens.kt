package com.example.decorra.ui.admin

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.decorra.R
import com.example.decorra.data.BankDetails
import com.example.decorra.data.BusinessProfile
import com.example.decorra.data.BusinessRepository
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.PrimaryButton

@Composable
private fun FormField(
    @StringRes label: Int,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    @StringRes error: Int? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(label)) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun BusinessProfileScreen(onBack: () -> Unit) {
    val saved = remember { BusinessRepository.profile.value }
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var phone by rememberSaveable { mutableStateOf(saved.phone) }
    var email by rememberSaveable { mutableStateOf(saved.email) }
    var address by rememberSaveable { mutableStateOf(saved.address) }
    var about by rememberSaveable { mutableStateOf(saved.about) }
    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val savedMessage = stringResource(R.string.changes_saved)

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.menu_business),
            navigationIcon = { BackButton(onBack) }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(R.string.business_name, name, { name = it; nameError = null }, error = nameError)
            FormField(R.string.business_phone, phone, { phone = it }, KeyboardType.Phone)
            FormField(R.string.business_email, email, { email = it }, KeyboardType.Email)
            FormField(R.string.business_address, address, { address = it })
            FormField(R.string.business_about, about, { about = it }, singleLine = false)
        }

        PrimaryButton(
            text = stringResource(R.string.save),
            onClick = {
                if (name.isBlank()) {
                    nameError = R.string.error_required
                } else {
                    BusinessRepository.saveProfile(
                        BusinessProfile(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            about = about.trim()
                        )
                    )
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    onBack()
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun BankDetailsScreen(onBack: () -> Unit) {
    val saved = remember { BusinessRepository.bank.value }
    var bankName by rememberSaveable { mutableStateOf(saved.bankName) }
    var holder by rememberSaveable { mutableStateOf(saved.accountHolder) }
    var number by rememberSaveable { mutableStateOf(saved.accountNumber) }
    var branch by rememberSaveable { mutableStateOf(saved.branchCode) }
    var reference by rememberSaveable { mutableStateOf(saved.reference) }
    var bankError by rememberSaveable { mutableStateOf<Int?>(null) }
    var holderError by rememberSaveable { mutableStateOf<Int?>(null) }
    var numberError by rememberSaveable { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val savedMessage = stringResource(R.string.changes_saved)

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.menu_payments),
            navigationIcon = { BackButton(onBack) }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.bank_helper), style = MaterialTheme.typography.bodyMedium)
            FormField(R.string.bank_name, bankName, { bankName = it; bankError = null }, error = bankError)
            FormField(R.string.account_holder, holder, { holder = it; holderError = null }, error = holderError)
            FormField(
                R.string.account_number,
                number,
                { if (it.length <= 20 && it.all(Char::isDigit)) { number = it; numberError = null } },
                KeyboardType.Number,
                error = numberError
            )
            FormField(
                R.string.branch_code,
                branch,
                { if (it.length <= 10 && it.all(Char::isDigit)) branch = it },
                KeyboardType.Number
            )
            FormField(R.string.payment_reference, reference, { reference = it }, singleLine = false)
        }

        PrimaryButton(
            text = stringResource(R.string.save),
            onClick = {
                bankError = if (bankName.isBlank()) R.string.error_required else null
                holderError = if (holder.isBlank()) R.string.error_required else null
                numberError = if (number.isBlank()) R.string.error_required else null
                if (bankError == null && holderError == null && numberError == null) {
                    BusinessRepository.saveBank(
                        BankDetails(
                            bankName = bankName.trim(),
                            accountHolder = holder.trim(),
                            accountNumber = number,
                            branchCode = branch,
                            reference = reference.trim()
                        )
                    )
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    onBack()
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}