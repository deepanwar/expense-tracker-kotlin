package com.example.expensetracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun equalSharesRemainderGoesToFirstPeople() {
        assertEquals(listOf(3334L, 3333L, 3333L), Money.equalShares(10_000, 3))
        assertEquals(listOf(60000L, 60000L, 60000L, 60000L), Money.equalShares(240_000, 4))
        assertEquals(emptyList<Long>(), Money.equalShares(100, 0))
    }

    @Test
    fun parseAndFormatRoundTrip() {
        assertEquals(10050L, Money.parseRupeesToPaise("100.50"))
        assertEquals(240000L, Money.parseRupeesToPaise("2,400"))
        assertEquals("100.50", Money.paiseToInput(10050))
        assertEquals("2400", Money.paiseToInput(240000))
        assertEquals("+₹100", Money.formatSignedPaise(10_000))
        assertEquals("-₹50", Money.formatSignedPaise(-5_000))
        assertEquals("₹0", Money.formatSignedPaise(0))
        assertNull(Money.parseRupeesToPaise("-10"))
        assertNull(Money.parseRupeesToPaise("abc"))
    }

    @Test
    fun sharesForPutsCurrentUserFirst() {
        val shares = Money.sharesFor(100, participantIds = listOf(3L, 1L, 2L), currentUserId = 2L)
        assertEquals(listOf(2L, 1L, 3L), shares.keys.toList())
        assertEquals(34L, shares[2L])
        assertEquals(33L, shares[1L])
        assertEquals(33L, shares[3L])
    }

    @Test
    fun exactSharesRequireFullTotal() {
        val valid = mapOf(1L to 500L, 2L to 400L, 3L to 300L)
        assertEquals(valid, Money.exactShares(1_200, valid))
        assertNull(Money.exactShares(1_200, mapOf(1L to 500L, 2L to 400L)))
        assertNull(Money.exactShares(1_200, mapOf(1L to -1L)))
        assertNull(Money.exactShares(1_200, emptyMap()))
    }

    @Test
    fun percentageSharesMustSumTo100() {
        val shares = Money.percentageShares(1_200, mapOf(1L to 50, 2L to 30, 3L to 20))
        assertEquals(mapOf(1L to 600L, 2L to 360L, 3L to 240L), shares)
        assertNull(Money.percentageShares(1_200, mapOf(1L to 50, 2L to 30)))
        assertNull(Money.percentageShares(1_200, emptyMap()))
    }

    @Test
    fun unitSharesSplitByWeight() {
        val shares = Money.unitShares(1_200, mapOf(1L to 2, 2L to 1, 3L to 1))
        assertEquals(mapOf(1L to 600L, 2L to 300L, 3L to 300L), shares)
        assertEquals(emptyMap<Long, Long>(), Money.unitShares(1_200, mapOf(1L to 0, 2L to 0)))
    }

    @Test
    fun reconstructInputsFromStoredAmounts() {
        val amounts = mapOf(1L to 600L, 2L to 300L, 3L to 300L)
        assertEquals(mapOf(1L to 50, 2L to 25, 3L to 25), Money.percentagesFromAmounts(amounts))
        assertEquals(mapOf(1L to 2, 2L to 1, 3L to 1), Money.shareUnitsFromAmounts(amounts))
    }
}
