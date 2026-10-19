package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QuoteDraft(
    val themeId: String? = null,
    val quantities: Map<String, Int> = emptyMap(),
    val eventEpochDay: Long? = null,
    val guests: Int? = null,
    val eventType: String = "",
    val venue: String = ""
)

fun QuoteDraft.lines(catalog: List<DecorItem>): List<QuoteLine> =
    catalog.mapNotNull { item ->
        val qty = quantities[item.id] ?: 0
        if (qty > 0) QuoteLine(item.id, item.name, item.priceCents, qty) else null
    }

object QuoteDraftRepository {
    /** Marks a draft that rents items on their own instead of customizing a theme. */
    const val RENTAL = "rental"

    private val _draft = MutableStateFlow(QuoteDraft())
    val draft: StateFlow<QuoteDraft> = _draft.asStateFlow()

    /** Keeps the current draft when the same theme is reopened, otherwise starts fresh. */
    fun start(themeId: String) {
        _draft.update { if (it.themeId == themeId) it else QuoteDraft(themeId = themeId) }
    }

    fun setQuantity(itemId: String, quantity: Int) {
        _draft.update {
            it.copy(
                quantities = if (quantity <= 0) it.quantities - itemId
                else it.quantities + (itemId to quantity.coerceAtMost(9999))
            )
        }
    }

    fun setEvent(epochDay: Long, guests: Int, eventType: String, venue: String) {
        _draft.update {
            it.copy(eventEpochDay = epochDay, guests = guests, eventType = eventType, venue = venue)
        }
    }

    fun clear() {
        _draft.value = QuoteDraft()
    }
}