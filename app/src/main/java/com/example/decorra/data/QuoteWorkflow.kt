package com.example.decorra.data

object QuoteWorkflow {

    /** A client submits a quote request. The decorator gets an alert. */
    fun submit(quote: Quote) {
        QuoteRepository.add(quote.copy(status = QuoteStatus.SUBMITTED))
        NotificationRepository.notifyAdmin(NotificationType.NEW_REQUEST, who(quote))
    }

    /** The decorator sends the quote to the client for approval. */
    fun sendToClient(quote: Quote) {
        QuoteRepository.markSent(quote.id)
        NotificationRepository.notifyClient(
            quote.clientEmail,
            NotificationType.QUOTE_UPDATE,
            quote.themeName
        )
    }

    /**
     * Changes the status. Returns the shortages that blocked it, or an empty list when it went
     * through. Stock is only checked when the quote starts holding stock.
     */
    fun changeStatus(quote: Quote, status: QuoteStatus, byClient: Boolean): List<Shortage> {
        if (status == quote.status) return emptyList()

        if (status in Inventory.RESERVING && quote.status !in Inventory.RESERVING) {
            val shortages = Inventory.shortages(
                quote,
                ItemRepository.items.value,
                QuoteRepository.quotes.value
            )
            if (shortages.isNotEmpty()) return shortages
        }

        QuoteRepository.setStatus(quote.id, status)

        if (byClient) {
            when (status) {
                QuoteStatus.APPROVED ->
                    NotificationRepository.notifyAdmin(NotificationType.CLIENT_APPROVED, who(quote))
                QuoteStatus.REJECTED ->
                    NotificationRepository.notifyAdmin(NotificationType.CLIENT_DECLINED, who(quote))
                else -> Unit
            }
        } else {
            val type = when (status) {
                QuoteStatus.APPROVED -> NotificationType.APPROVED
                QuoteStatus.REJECTED -> NotificationType.DECLINED
                QuoteStatus.DEPOSIT_PAID -> NotificationType.DEPOSIT
                else -> null
            }
            if (type != null) {
                NotificationRepository.notifyClient(quote.clientEmail, type, quote.themeName)
            }
        }
        return emptyList()
    }

    private fun who(quote: Quote) = quote.clientName.ifBlank { quote.clientEmail }
}