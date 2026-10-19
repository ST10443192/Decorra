package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.util.UUID

/** clientEmail identifies the conversation: one thread per client. */
data class ChatMessage(
    val id: String,
    val text: String,
    val fromClient: Boolean,
    val timeMillis: Long,
    val clientEmail: String = "",
    val fileId: String? = null,
    val fileName: String? = null,
    val fileMime: String? = null
)

object NotificationType {
    const val QUOTE_UPDATE = "quote_update"
    const val DEPOSIT = "deposit"
    const val APPROVED = "approved"
    const val DECLINED = "declined"
    const val NEW_REQUEST = "new_request"
    const val CLIENT_APPROVED = "client_approved"
    const val CLIENT_DECLINED = "client_declined"
}

/**
 * Typed notifications are turned into text when they are shown, so they follow the app language.
 * Older ones without a type keep their stored title and body.
 * clientEmail is the client it is for; forAdmin = true means it is for the decorators instead.
 */
data class AppNotification(
    val id: String,
    val title: String = "",
    val body: String = "",
    val timeMillis: Long,
    val clientEmail: String = "",
    val type: String = "",
    val arg: String = "",
    val forAdmin: Boolean = false,
    val read: Boolean = false
)

object MessageRepository {
    private const val FILE = "messages.json"

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching { _messages.value = JSONArray(text).objects().map { it.toChatMessage() } }
        }
    }

    /** A client writing to the decorator. */
    fun send(clientEmail: String, text: String) = add(clientEmail, text, fromClient = true)

    /** The decorator replying to one client. */
    fun reply(clientEmail: String, text: String) = add(clientEmail, text, fromClient = false)

    private fun add(clientEmail: String, text: String, fromClient: Boolean) {
        _messages.update {
            it + ChatMessage(
                id = UUID.randomUUID().toString(),
                text = text,
                fromClient = fromClient,
                timeMillis = System.currentTimeMillis(),
                clientEmail = clientEmail
            )
        }
        Storage.write(FILE, _messages.value.toJsonArray { it.toJson() }.toString())
    }
}

object NotificationRepository {
    private const val FILE = "notifications.json"

    private val _items = MutableStateFlow<List<AppNotification>>(emptyList())
    val items: StateFlow<List<AppNotification>> = _items.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching { _items.value = JSONArray(text).objects().map { it.toNotification() } }
        }
    }

    fun add(notification: AppNotification) {
        _items.update { it + notification }
        save()
    }

    fun notifyClient(email: String, type: String, arg: String = "") {
        if (email.isBlank()) return
        add(
            AppNotification(
                id = UUID.randomUUID().toString(),
                timeMillis = System.currentTimeMillis(),
                clientEmail = email,
                type = type,
                arg = arg
            )
        )
    }

    fun notifyAdmin(type: String, arg: String = "") {
        add(
            AppNotification(
                id = UUID.randomUUID().toString(),
                timeMillis = System.currentTimeMillis(),
                type = type,
                arg = arg,
                forAdmin = true
            )
        )
    }

    fun markAdminRead() {
        _items.update { list ->
            list.map { if (it.forAdmin && !it.read) it.copy(read = true) else it }
        }
        save()
    }

    private fun save() =
        Storage.write(FILE, _items.value.toJsonArray { it.toJson() }.toString())
}