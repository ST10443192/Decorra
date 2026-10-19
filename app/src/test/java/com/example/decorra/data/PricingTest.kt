package com.example.decorra.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingTest {

    @Test
    fun vat_isFifteenPercent() {
        assertEquals(1_500L, Pricing.vat(10_000L))
    }

    @Test
    fun vat_roundsHalfUpToTheNearestCent() {
        assertEquals(2L, Pricing.vat(10L)) // 1.5 cents rounds up
        assertEquals(1L, Pricing.vat(9L))  // 1.35 cents rounds down
    }

    @Test
    fun vat_forTheWireframeSubtotal() {
        // R8 152.00 subtotal gives R1 222.80 VAT
        assertEquals(122_280L, Pricing.vat(815_200L))
    }

    @Test
    fun deposit_isFortyPercent() {
        assertEquals(40_000L, Pricing.deposit(100_000L))
        assertEquals(50L, Pricing.deposit(125L))
    }

    @Test
    fun zeroStaysZero() {
        assertEquals(0L, Pricing.vat(0L))
        assertEquals(0L, Pricing.deposit(0L))
    }
}