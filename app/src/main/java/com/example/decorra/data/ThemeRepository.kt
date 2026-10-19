package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray

object ThemeRepository {
    private const val FILE = "themes.json"

    private val _themes = MutableStateFlow<List<DecorTheme>>(emptyList())
    val themes: StateFlow<List<DecorTheme>> = _themes.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching { _themes.value = JSONArray(text).objects().map { it.toTheme() } }
        }
    }

    fun add(theme: DecorTheme) {
        _themes.update { it + theme }
        save()
    }

    fun remove(id: String) {
        _themes.value.firstOrNull { it.id == id }?.let { ImageStore.delete(it.imageUri) }
        _themes.update { list -> list.filterNot { it.id == id } }
        save()
    }

    private fun save() =
        Storage.write(FILE, _themes.value.toJsonArray { it.toJson() }.toString())
}