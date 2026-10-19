package com.example.decorra.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.util.UUID

object QuoteRepository {
    private const val FILE = "quotes.json"

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    fun load() {
        Storage.read(FILE)?.let { text ->
            runCatching { _quotes.value = JSONArray(text).objects().map { it.toQuote() } }
        }
    }

    fun add(quote: Quote) {
        _quotes.update { it + quote }
        save()
    }

    fun toggleTask(quoteId: String, taskId: String) = mutate(quoteId) { quote ->
        quote.copy(
            tasks = quote.tasks.map { task ->
                if (task.id == taskId) task.copy(done = !task.done) else task
            }
        )
    }

    fun addTask(quoteId: String, title: String) = mutate(quoteId) { quote ->
        quote.copy(tasks = quote.tasks + QuoteTask(UUID.randomUUID().toString(), title))
    }

    fun removeTask(quoteId: String, taskId: String) = mutate(quoteId) { quote ->
        quote.copy(tasks = quote.tasks.filterNot { it.id == taskId })
    }

    /** Use QuoteWorkflow.changeStatus from screens: it also checks stock and notifies. */
    fun setStatus(quoteId: String, status: QuoteStatus) = mutate(quoteId) {
        it.copy(
            status = status,
            sentToClient = if (status == QuoteStatus.SUBMITTED) it.sentToClient else false
        )
    }

    fun markSent(quoteId: String) = mutate(quoteId) { it.copy(sentToClient = true) }

    fun setNote(quoteId: String, note: String) =
        mutate(quoteId) { it.copy(note = note) }

    /**
     * quantity 0 removes the line; an unknown item is appended.
     * Changing a quote that is waiting for the client's answer means it must be sent again.
     */
    fun setQuantity(quoteId: String, item: DecorItem, quantity: Int) = mutate(quoteId) { quote ->
        val exists = quote.lines.any { it.itemId == item.id }
        val lines = when {
            quantity <= 0 -> quote.lines.filterNot { it.itemId == item.id }
            exists -> quote.lines.map {
                if (it.itemId == item.id) it.copy(quantity = quantity.coerceAtMost(9999)) else it
            }
            else -> quote.lines + QuoteLine(item.id, item.name, item.priceCents, quantity.coerceAtMost(9999))
        }
        quote.copy(
            lines = lines,
            sentToClient = if (quote.status == QuoteStatus.SUBMITTED) false else quote.sentToClient
        )
    }

    private fun mutate(quoteId: String, change: (Quote) -> Quote) {
        _quotes.update { list -> list.map { if (it.id == quoteId) change(it) else it } }
        save()
    }

    private fun save() =
        Storage.write(FILE, _quotes.value.toJsonArray { it.toJson() }.toString())
}