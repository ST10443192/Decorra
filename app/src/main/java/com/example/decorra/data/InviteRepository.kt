package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

data class Invite(
    val code: String,
    val createdMillis: Long,
    val usedBy: String? = null
)

/** One-time codes that let a new decorator join. Stored on this device. */
object InviteRepository {
    private const val FILE = "invites.json"

    // No 0, O, 1 or I, so codes read out loud or typed from a message are hard to mix up.
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    private val _invites = MutableStateFlow<List<Invite>>(emptyList())
    val invites: StateFlow<List<Invite>> = _invites.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching {
                _invites.value = JSONArray(text).objects().map {
                    Invite(
                        code = it.getString("code"),
                        createdMillis = it.getLong("createdMillis"),
                        usedBy = it.stringOrNull("usedBy")
                    )
                }
            }
        }
    }

    fun create(): Invite {
        val random = SecureRandom()
        val raw = buildString { repeat(8) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }
        val invite = Invite("${raw.take(4)}-${raw.drop(4)}", System.currentTimeMillis())
        _invites.update { it + invite }
        save()
        return invite
    }

    fun isValid(input: String): Boolean = find(input)?.usedBy == null && find(input) != null

    /** Marks the code as used by [email]. Returns false if it does not exist or was used. */
    fun redeem(input: String, email: String): Boolean {
        val invite = find(input) ?: return false
        if (invite.usedBy != null) return false
        _invites.update { list ->
            list.map { if (it.code == invite.code) it.copy(usedBy = email) else it }
        }
        save()
        return true
    }

    private fun normalize(text: String) = text.uppercase().filter { it.isLetterOrDigit() }

    private fun find(input: String): Invite? {
        val wanted = normalize(input)
        if (wanted.isEmpty()) return null
        return _invites.value.firstOrNull { normalize(it.code) == wanted }
    }

    private fun save() {
        val array = _invites.value.toJsonArray {
            JSONObject()
                .put("code", it.code)
                .put("createdMillis", it.createdMillis)
                .put("usedBy", it.usedBy)
        }
        Storage.write(FILE, array.toString())
    }
}