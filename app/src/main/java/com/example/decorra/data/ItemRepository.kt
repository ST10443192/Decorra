package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray

object ItemRepository {
    private const val FILE = "items.json"

    private val _items = MutableStateFlow<List<DecorItem>>(emptyList())
    val items: StateFlow<List<DecorItem>> = _items.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching { _items.value = JSONArray(text).objects().map { it.toItem() } }
        }
    }

    fun add(item: DecorItem) {
        _items.update { it + item }
        save()
    }

    fun update(item: DecorItem) {
        _items.update { list -> list.map { if (it.id == item.id) item else it } }
        save()
    }

    /** Adds to the stock. An item that was not tracked starts counting from zero. */
    fun restock(id: String, quantity: Int) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(stock = (it.stock ?: 0) + quantity) else it }
        }
        save()
    }

    fun remove(id: String) {
        _items.value.firstOrNull { it.id == id }?.let { ImageStore.delete(it.imageUri) }
        _items.update { list -> list.filterNot { it.id == id } }
        save()
    }

    private fun save() =
        Storage.write(FILE, _items.value.toJsonArray { it.toJson() }.toString())
}