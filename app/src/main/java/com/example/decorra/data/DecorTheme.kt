package com.example.decorra.data

data class DecorTheme(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val imageUri: String? = null
)