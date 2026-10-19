package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

enum class NotificationCategory(val key: String) {
    QUOTE_UPDATES("quote_updates"),
    PAYMENTS("payments"),
    COUNTDOWN("countdown"),
    NEW_REQUESTS("new_requests"),
    CLIENT_RESPONSES("client_responses"),
    LOW_STOCK("low_stock")
}

/** Older notifications without a type have no category, so they always show. */
fun AppNotification.category(): NotificationCategory? = when (type) {
    NotificationType.QUOTE_UPDATE,
    NotificationType.APPROVED,
    NotificationType.DECLINED -> NotificationCategory.QUOTE_UPDATES
    NotificationType.DEPOSIT -> NotificationCategory.PAYMENTS
    NotificationType.NEW_REQUEST -> NotificationCategory.NEW_REQUESTS
    NotificationType.CLIENT_APPROVED,
    NotificationType.CLIENT_DECLINED -> NotificationCategory.CLIENT_RESPONSES
    else -> null
}

/** True unless this person has switched the category off. */
fun Map<String, Set<String>>.allows(email: String, category: NotificationCategory?): Boolean =
    category == null || category.key !in (this[email.lowercase()] ?: emptySet())

/** What each person has switched off, keyed by lower-case email. Stored on this device. */
object NotificationPreferences {
    private const val FILE = "preferences.json"

    private val _state = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val state: StateFlow<Map<String, Set<String>>> = _state.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching {
                val root = JSONObject(text)
                val loaded = mutableMapOf<String, Set<String>>()
                root.keys().forEach { email ->
                    val array = root.getJSONArray(email)
                    loaded[email] = List(array.length()) { array.getString(it) }.toSet()
                }
                _state.value = loaded
            }
        }
    }

    fun setEnabled(email: String, category: NotificationCategory, enabled: Boolean) {
        val key = email.lowercase()
        _state.update { current ->
            val disabled = current[key].orEmpty()
            val updated = if (enabled) disabled - category.key else disabled + category.key
            current + (key to updated)
        }
        save()
    }

    private fun save() {
        val root = JSONObject()
        _state.value.forEach { (email, disabled) ->
            root.put(email, JSONArray(disabled.toList()))
        }
        Storage.write(FILE, root.toString())
    }
}