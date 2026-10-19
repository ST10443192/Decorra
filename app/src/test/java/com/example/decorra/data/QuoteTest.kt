package com.example.decorra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteTest {

    private fun quote(lines: List<QuoteLine>) = Quote(
        id = "q1",
        themeId = "t1",
        themeName = "Traditional",
        eventEpochDay = 100L,
        guests = 120,
        lines = lines,
        status = QuoteStatus.SUBMITTED
    )

    @Test
    fun lineTotal_isPriceTimesQuantity() {
        assertEquals(420_000L, QuoteLine("chairs", "Wooden chairs", 3_500L, 120).totalCents)
    }

    @Test
    fun totals_addVatToTheSubtotal() {
        val q = quote(
            listOf(
                QuoteLine("chairs", "Wooden chairs", 3_500L, 120), // 420 000
                QuoteLine("cloths", "Tablecloths", 500L, 12)       // 6 000
            )
        )
        assertEquals(426_000L, q.subtotalCents)
        assertEquals(63_900L, q.vatCents)
        assertEquals(489_900L, q.totalCents)
    }

    @Test
    fun aQuoteWithNoLinesCostsNothing() {
        assertEquals(0L, quote(emptyList()).totalCents)
    }

    @Test
    fun rejectedComesBeforeApprovedSoOrdinalChecksLeaveItOut() {
        assertTrue(QuoteStatus.REJECTED.ordinal < QuoteStatus.APPROVED.ordinal)
        assertTrue(QuoteStatus.REJECTED.ordinal < QuoteStatus.DEPOSIT_PAID.ordinal)
    }

    @Test
    fun onlyApprovedAndLaterHoldStock() {
        assertEquals(
            setOf(QuoteStatus.APPROVED, QuoteStatus.DEPOSIT_PAID, QuoteStatus.IN_PROGRESS),
            Inventory.RESERVING
        )
    }
}