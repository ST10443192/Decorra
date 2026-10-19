package com.example.decorra.ui.client

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.decorra.R
import com.example.decorra.data.AccountRepository
import com.example.decorra.data.ActionResult
import com.example.decorra.data.NotificationCategory
import com.example.decorra.data.NotificationPreferences
import com.example.decorra.data.UserSession
import com.example.decorra.data.allows
import com.example.decorra.ui.components.BackButton
import com.example.decorra.ui.components.DecorraTopBar
import com.example.decorra.ui.components.InfoCard
import com.example.decorra.ui.components.PrimaryButton
import com.example.decorra.ui.components.SecondaryButton
import com.example.decorra.util.LanguageManager
import com.example.decorra.util.UserRole

@Composable
private fun SettingsField(
    @StringRes label: Int,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    @StringRes error: Int? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    @StringRes label: Int,
    value: String,
    onChange: (String) -> Unit,
    @StringRes error: Int?
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (visible) R.string.hide_password else R.string.show_password
                    )
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun EditProfilePage(onBack: () -> Unit) {
    val context = LocalContext.current
    val updatedMessage = stringResource(R.string.profile_updated)

    var first by rememberSaveable { mutableStateOf(UserSession.firstName) }
    var last by rememberSaveable { mutableStateOf(UserSession.lastName) }
    var phone by rememberSaveable { mutableStateOf(UserSession.phone) }
    var firstError by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastError by rememberSaveable { mutableStateOf<Int?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.settings_edit_profile),
            navigationIcon = { BackButton(onBack) }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsField(R.string.first_name, first, { first = it; firstError = null }, error = firstError)
            SettingsField(R.string.last_name, last, { last = it; lastError = null }, error = lastError)
            SettingsField(
                R.string.phone_number,
                phone,
                { phone = it; phoneError = null },
                KeyboardType.Phone,
                phoneError
            )
            OutlinedTextField(
                value = UserSession.email,
                onValueChange = {},
                enabled = false,
                label = { Text(stringResource(R.string.email_address)) },
                supportingText = { Text(stringResource(R.string.email_cannot_change)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        PrimaryButton(
            text = stringResource(R.string.save),
            onClick = {
                firstError = if (first.isBlank()) R.string.error_required else null
                lastError = if (last.isBlank()) R.string.error_required else null
                phoneError = when {
                    phone.isBlank() -> R.string.error_required
                    phone.count { it.isDigit() } !in 9..15 -> R.string.error_phone_invalid
                    else -> null
                }
                if (firstError == null && lastError == null && phoneError == null) {
                    AccountRepository.updateProfile(first, last, phone)
                    Toast.makeText(context, updatedMessage, Toast.LENGTH_SHORT).show()
                    onBack()
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun ChangePasswordPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val updatedMessage = stringResource(R.string.password_updated)
    val networkMessage = stringResource(R.string.error_network)
    val genericMessage = stringResource(R.string.error_generic)

    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var currentError by rememberSaveable { mutableStateOf<Int?>(null) }
    var newError by rememberSaveable { mutableStateOf<Int?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<Int?>(null) }
    var busy by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.settings_change_password),
            navigationIcon = { BackButton(onBack) }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PasswordField(
                R.string.current_password, currentPassword,
                { currentPassword = it; currentError = null }, currentError
            )
            PasswordField(
                R.string.new_password, newPassword,
                { newPassword = it; newError = null }, newError
            )
            PasswordField(
                R.string.confirm_password, confirm,
                { confirm = it; confirmError = null }, confirmError
            )
        }

        PrimaryButton(
            text = stringResource(R.string.settings_change_password),
            enabled = !busy,
            onClick = {
                currentError = if (currentPassword.isBlank()) R.string.error_required else null
                newError = when {
                    newPassword.isBlank() -> R.string.error_required
                    newPassword.length < 8 -> R.string.error_password_short
                    newPassword == currentPassword -> R.string.error_password_same
                    else -> null
                }
                confirmError = when {
                    confirm.isBlank() -> R.string.error_required
                    confirm != newPassword -> R.string.error_password_mismatch
                    else -> null
                }
                if (currentError == null && newError == null && confirmError == null) {
                    busy = true
                    AccountRepository.changePassword(currentPassword, newPassword) { result ->
                        busy = false
                        when (result) {
                            ActionResult.SUCCESS -> {
                                Toast.makeText(context, updatedMessage, Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                            ActionResult.WRONG_PASSWORD -> currentError = R.string.error_current_password
                            ActionResult.NETWORK ->
                                Toast.makeText(context, networkMessage, Toast.LENGTH_LONG).show()
                            ActionResult.FAILED ->
                                Toast.makeText(context, genericMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}

private data class PreferenceOption(
    val category: NotificationCategory,
    @StringRes val title: Int,
    @StringRes val description: Int
)

private val clientOptions = listOf(
    PreferenceOption(NotificationCategory.QUOTE_UPDATES, R.string.pref_quote_updates, R.string.pref_quote_updates_desc),
    PreferenceOption(NotificationCategory.PAYMENTS, R.string.pref_payments, R.string.pref_payments_desc),
    PreferenceOption(NotificationCategory.COUNTDOWN, R.string.pref_countdown, R.string.pref_countdown_desc)
)

private val decoratorOptions = listOf(
    PreferenceOption(NotificationCategory.NEW_REQUESTS, R.string.pref_new_requests, R.string.pref_new_requests_desc),
    PreferenceOption(NotificationCategory.CLIENT_RESPONSES, R.string.pref_client_responses, R.string.pref_client_responses_desc),
    PreferenceOption(NotificationCategory.LOW_STOCK, R.string.pref_low_stock, R.string.pref_low_stock_desc)
)

/** Whether Android lets Decorra show notifications. Re-checked when the person comes back. */
@Composable
private fun rememberSystemNotificationsEnabled(): Boolean {
    val context = LocalContext.current
    var enabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val owner = context as? LifecycleOwner
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }
    return enabled
}

private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
    }
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@Composable
private fun PreferenceRow(
    @StringRes title: Int,
    @StringRes description: Int,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(description), style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
fun NotificationPreferencesPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs by NotificationPreferences.state.collectAsState()
    val email = UserSession.email
    val account = AccountRepository.currentAccount()
    val options = if (account?.role == UserRole.DECORATOR) decoratorOptions else clientOptions

    var pushOn by rememberSaveable { mutableStateOf(account?.pushMessages ?: true) }
    val systemEnabled = rememberSystemNotificationsEnabled()

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.settings_notifications),
            navigationIcon = { BackButton(onBack) }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.notif_prefs_hint),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))

            if (!systemEnabled) {
                InfoCard(R.string.system_notifications_off_title) {
                    Text(
                        stringResource(R.string.system_notifications_off_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SecondaryButton(
                        text = stringResource(R.string.open_phone_settings),
                        onClick = { openNotificationSettings(context) }
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Push messages are decided by the server, so this one is saved to the profile.
            PreferenceRow(
                title = R.string.pref_messages,
                description = R.string.pref_messages_desc,
                checked = pushOn
            ) {
                pushOn = it
                AccountRepository.setPushMessages(it)
            }

            options.forEach { option ->
                PreferenceRow(
                    title = option.title,
                    description = option.description,
                    checked = prefs.allows(email, option.category)
                ) { NotificationPreferences.setEnabled(email, option.category, it) }
            }
        }
    }
}

private data class LanguageOption(val tag: String, @StringRes val label: Int)

private val languageOptions = listOf(
    LanguageOption(LanguageManager.SYSTEM, R.string.language_system),
    LanguageOption("en", R.string.language_english),
    LanguageOption("af", R.string.language_afrikaans),
    LanguageOption("zu", R.string.language_zulu)
)

@Composable
fun LanguagePage(onBack: () -> Unit) {
    var selected by remember { mutableStateOf(LanguageManager.selectedTag()) }

    Column(Modifier.fillMaxSize()) {
        DecorraTopBar(
            title = stringResource(R.string.language_heading),
            navigationIcon = { BackButton(onBack) }
        )

        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.language_hint), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            languageOptions.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option.tag,
                            onClick = {
                                selected = option.tag
                                LanguageManager.set(option.tag)
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == option.tag, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(option.label))
                }
            }
        }
    }
}