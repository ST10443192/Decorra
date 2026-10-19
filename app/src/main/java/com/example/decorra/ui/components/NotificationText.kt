package com.example.decorra.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.decorra.R
import com.example.decorra.data.AppNotification
import com.example.decorra.data.NotificationType

@Composable
fun AppNotification.displayTitle(): String = when (type) {
    NotificationType.QUOTE_UPDATE -> stringResource(R.string.notif_quote_update_title)
    NotificationType.DEPOSIT -> stringResource(R.string.notif_deposit_title)
    NotificationType.APPROVED -> stringResource(R.string.notif_approved_title)
    NotificationType.DECLINED -> stringResource(R.string.notif_declined_title)
    NotificationType.NEW_REQUEST -> stringResource(R.string.notif_new_request_title)
    NotificationType.CLIENT_APPROVED -> stringResource(R.string.notif_client_approved_title)
    NotificationType.CLIENT_DECLINED -> stringResource(R.string.notif_client_declined_title)
    else -> title
}

@Composable
fun AppNotification.displayBody(): String = when (type) {
    NotificationType.QUOTE_UPDATE -> stringResource(R.string.notif_quote_update_body)
    NotificationType.DEPOSIT -> stringResource(R.string.notif_deposit_body, arg)
    NotificationType.APPROVED -> stringResource(R.string.notif_approved_body, arg)
    NotificationType.DECLINED -> stringResource(R.string.notif_declined_body, arg)
    NotificationType.NEW_REQUEST -> stringResource(R.string.notif_new_request_body, arg)
    NotificationType.CLIENT_APPROVED -> stringResource(R.string.notif_client_approved_body, arg)
    NotificationType.CLIENT_DECLINED -> stringResource(R.string.notif_client_declined_body, arg)
    else -> body
}