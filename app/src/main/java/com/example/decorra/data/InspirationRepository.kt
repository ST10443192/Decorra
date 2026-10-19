package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class InspirationPhoto(
    val id: String,
    val path: String,
    val clientEmail: String
)

/** Each client's personal photo board. Stored on this device. */
object InspirationRepository {
    private const val FILE = "inspiration.json"

    private val _photos = MutableStateFlow<List<InspirationPhoto>>(emptyList())
    val photos: StateFlow<List<InspirationPhoto>> = _photos.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching {
                _photos.value = JSONArray(text).objects().map {
                    InspirationPhoto(
                        id = it.getString("id"),
                        path = it.getString("path"),
                        clientEmail = it.getString("clientEmail")
                    )
                }
            }
        }
    }

    fun add(clientEmail: String, path: String) {
        _photos.update { it + InspirationPhoto(UUID.randomUUID().toString(), path, clientEmail) }
        save()
    }

    fun remove(id: String) {
        _photos.value.firstOrNull { it.id == id }?.let { ImageStore.delete(it.path) }
        _photos.update { list -> list.filterNot { it.id == id } }
        save()
    }

    private fun save() {
        val array = _photos.value.toJsonArray {
            JSONObject()
                .put("id", it.id)
                .put("path", it.path)
                .put("clientEmail", it.clientEmail)
        }
        Storage.write(FILE, array.toString())
    }
}