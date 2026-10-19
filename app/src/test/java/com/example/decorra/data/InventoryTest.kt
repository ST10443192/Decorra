package com.example.decorra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryTest {

    private val chairs = DecorItem("chairs", "Wooden chairs", 3_500L, stock = 100, minStock = 30)
    private val tent = DecorItem("tent", "Open-sided tent", 200_000L) // stock not tracked
    private val items = listOf(chairs, tent)
    private val day = 50L

    private fun quote(
        id: String,
        status: QuoteStatus,
        chairsQty: Int,
        eventDay: Long = day
    ) = Quote(
        id = id,
        themeId = "t1",
        themeName = "Traditional",
        eventEpochDay = eventDay,
        guests = 100,
        lines = listOf(QuoteLine("chairs", "Wooden chairs", 3_500L, chairsQty)),
        status = status
    )

    private fun request(qty: Int) = listOf(QuoteLine("chairs", "Wooden chairs", 3_500L, qty))

    @Test
    fun anApprovedQuoteReservesStockOnItsDate() {
        val quotes = listOf(quote("a", QuoteStatus.APPROVED, 60))
        val result = Inventory.shortages(request(50), day, items, quotes)
        assertEquals(listOf(Shortage("Wooden chairs", 50, 40)), result)
    }

    @Test
    fun askingForExactlyWhatIsFreeIsFine() {
        val quotes = listOf(quote("a", QuoteStatus.APPROVED, 60))
        assertTrue(Inventory.shortages(request(40), day, items, quotes).isEmpty())
    }

    @Test
    fun reservationsOnOtherDaysDoNotCount() {
        val quotes = listOf(quote("a", QuoteStatus.APPROVED, 90, eventDay = day + 1))
        assertTrue(Inventory.shortages(request(100), day, items, quotes).isEmpty())
    }

    @Test
    fun pendingRejectedAndDraftQuotesDoNotHoldStock() {
        val quotes = listOf(
            quote("a", QuoteStatus.SUBMITTED, 90),
            quote("b", QuoteStatus.REJECTED, 90),
            quote("c", QuoteStatus.DRAFT, 90)
        )
        assertTrue(Inventory.shortages(request(100), day, items, quotes).isEmpty())
    }

    @Test
    fun severalReservationsOnTheSameDayAddUp() {
        val quotes = listOf(
            quote("a", QuoteStatus.APPROVED, 60),
            quote("b", QuoteStatus.DEPOSIT_PAID, 10)
        )
        assertEquals(70, Inventory.reserved(quotes, "chairs", day))
        assertEquals(30, Inventory.shortages(request(31), day, items, quotes).single().available)
    }

    @Test
    fun aQuoteDoesNotCompeteWithItself() {
        val own = quote("a", QuoteStatus.APPROVED, 60)
        assertTrue(Inventory.shortages(own.lines, day, items, listOf(own), excludeQuoteId = "a").isEmpty())
    }

    @Test
    fun itemsWithoutTrackedStockAreNeverLimited() {
        val lines = listOf(QuoteLine("tent", "Open-sided tent", 200_000L, 50))
        assertTrue(Inventory.shortages(lines, day, items, emptyList()).isEmpty())
    }

    @Test
    fun stockLevels() {
        fun level(stock: Int?, min: Int) =
            DecorItem("x", "X", 100L, stock = stock, minStock = min).stockLevel()

        assertEquals(StockLevel.NOT_TRACKED, level(null, 30))
        assertEquals(StockLevel.CRITICAL, level(0, 30))
        assertEquals(StockLevel.CRITICAL, level(15, 30)) // at half the minimum
        assertEquals(StockLevel.LOW, level(16, 30))
        assertEquals(StockLevel.LOW, level(30, 30))
        assertEquals(StockLevel.HEALTHY, level(31, 30))
        assertEquals(StockLevel.HEALTHY, level(5, 0))    // no minimum set
        assertEquals(StockLevel.CRITICAL, level(0, 0))   // sold out
    }

    @Test
    fun totalValueIsStockTimesUnitCost() {
        assertEquals(2_500L, DecorItem("x", "X", 100L, stock = 10, unitCostCents = 250L).totalValueCents)
        assertEquals(0L, DecorItem("x", "X", 100L, unitCostCents = 250L).totalValueCents)
    }
}