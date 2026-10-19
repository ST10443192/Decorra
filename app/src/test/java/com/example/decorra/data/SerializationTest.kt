package com.example.decorra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SerializationTest {

    private val quote = Quote(
        id = "q1",
        themeId = "t1",
        themeName = "Traditional",
        eventEpochDay = 20_000L,
        guests = 120,
        lines = listOf(QuoteLine("chairs", "Wooden chairs", 3_500L, 120)),
        status = QuoteStatus.APPROVED,
        tasks = listOf(QuoteTask("k1", "Confirm guest count", done = true)),
        clientName = "Thandeka Nkosi",
        clientEmail = "thandeka@email.com",
        clientPhone = "+27721234567",
        note = "Brown colour scheme",
        eventType = "Wedding",
        venue = "The Manor House",
        sentToClient = true
    )

    @Test
    fun aQuoteSurvivesBeingSavedAndLoaded() {
        assertEquals(quote, quote.toJson().toQuote())
    }

    @Test
    fun quotesSavedByOlderVersionsLoadWithDefaults() {
        val old = quote.toJson()
        old.remove("eventType")
        old.remove("venue")
        old.remove("sentToClient")

        val loaded = old.toQuote()
        assertEquals("", loaded.eventType)
        assertEquals("", loaded.venue)
        assertFalse(loaded.sentToClient)
    }

    @Test
    fun anItemWithTrackedStockSurvives() {
        val item = DecorItem(
            id = "chairs", name = "Wooden chairs", priceCents = 3_500L, themeId = "t1",
            rentable = true, stock = 100, minStock = 30, unitCostCents = 2_000L
        )
        assertEquals(item, item.toJson().toItem())
    }

    @Test
    fun anItemWithoutStockStaysUntracked() {
        val item = DecorItem("tent", "Open-sided tent", 200_000L)
        val loaded = item.toJson().toItem()
        assertNull(loaded.stock)
        assertEquals(item, loaded)
    }
}