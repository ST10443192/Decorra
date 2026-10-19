package com.example.decorra.data

/**
 * themeId = null means the item is available inside every theme.
 * rentable = true also lets clients rent it on its own, without a theme.
 * stock = null means the quantity is not tracked, so the item is never limited.
 */
data class DecorItem(
    val id: String,
    val name: String,
    val priceCents: Long,
    val themeId: String? = null,
    val imageUri: String? = null,
    val rentable: Boolean = false,
    val stock: Int? = null,
    val minStock: Int = 0,
    val unitCostCents: Long = 0
)