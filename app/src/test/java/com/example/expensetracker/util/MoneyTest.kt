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
}
