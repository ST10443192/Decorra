package com.example.decorra.data

object Pricing {
    const val VAT_PERCENT = 15
    const val DEPOSIT_PERCENT = 40

    fun vat(subtotalCents: Long): Long = (subtotalCents * VAT_PERCENT + 50) / 100
    fun deposit(totalCents: Long): Long = (totalCents * DEPOSIT_PERCENT + 50) / 100
}

/** Order matters: the status stepper and the ordinal checks follow it. */
enum class QuoteStatus { DRAFT, SUBMITTED, REJECTED, APPROVED, DEPOSIT_PAID, IN_PROGRESS, COMPLETE }

data class QuoteLine(
    val itemId: String,
    val name: String,
    val unitPriceCents: Long,
    val quantity: Int
) {
    val totalCents: Long get() = unitPriceCents * quantity
}

data class QuoteTask(
    val id: String,
    val title: String,
    val done: Boolean = false
)

data class Quote(
    val id: String,
    val themeId: String,
    val themeName: String,
    val eventEpochDay: Long,
    val guests: Int,
    val lines: List<QuoteLine>,
    val status: QuoteStatus,
    val tasks: List<QuoteTask> = emptyList(),
    val clientName: String = "",
    val clientEmail: String = "",
    val clientPhone: String = "",
    val note: String = "",
    val eventType: String = "",
    val venue: String = "",
    /** True once the decorator has sent the quote to the client for approval. */
    val sentToClient: Boolean = false
) {
    val subtotalCents: Long get() = lines.sumOf { it.totalCents }
    val vatCents: Long get() = Pricing.vat(subtotalCents)
    val totalCents: Long get() = subtotalCents + vatCents
}