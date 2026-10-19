package com.example.decorra.data

import kotlin.math.max

/** An item that cannot cover the quantity a quote asks for on its event date. */
data class Shortage(val itemName: String, val requested: Int, val available: Int)

enum class StockLevel { NOT_TRACKED, HEALTHY, LOW, CRITICAL }

/** Critical: sold out, or at or below half the minimum. Low: at or below the minimum. */
fun DecorItem.stockLevel(): StockLevel {
    val current = stock ?: return StockLevel.NOT_TRACKED
    return when {
        current <= 0 -> StockLevel.CRITICAL
        minStock > 0 && current * 2 <= minStock -> StockLevel.CRITICAL
        minStock > 0 && current <= minStock -> StockLevel.LOW
        else -> StockLevel.HEALTHY
    }
}

val DecorItem.totalValueCents: Long
    get() = (stock ?: 0).toLong() * unitCostCents

object Inventory {
    /** Quotes in these statuses hold stock on their event date. */
    val RESERVING = setOf(QuoteStatus.APPROVED, QuoteStatus.DEPOSIT_PAID, QuoteStatus.IN_PROGRESS)

    fun reserved(
        quotes: List<Quote>,
        itemId: String,
        day: Long,
        excludeQuoteId: String? = null
    ): Int = quotes
        .filter { it.id != excludeQuoteId && it.status in RESERVING && it.eventEpochDay == day }
        .sumOf { quote -> quote.lines.filter { it.itemId == itemId }.sumOf { it.quantity } }

    fun shortages(
        lines: List<QuoteLine>,
        day: Long,
        items: List<DecorItem>,
        quotes: List<Quote>,
        excludeQuoteId: String? = null
    ): List<Shortage> = lines.mapNotNull { line ->
        val stock = items.firstOrNull { it.id == line.itemId }?.stock ?: return@mapNotNull null
        val free = max(0, stock - reserved(quotes, line.itemId, day, excludeQuoteId))
        if (line.quantity > free) Shortage(line.name, line.quantity, free) else null
    }

    fun shortages(quote: Quote, items: List<DecorItem>, quotes: List<Quote>): List<Shortage> =
        shortages(quote.lines, quote.eventEpochDay, items, quotes, quote.id)
}