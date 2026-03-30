package com.example.whistrentzscorer

import com.example.whistrentzscorer.components.enabledCondition
import com.example.whistrentzscorer.components.getIllegalChoice
import com.example.whistrentzscorer.components.handsTakenSoFar
import com.example.whistrentzscorer.components.isLastPlayer
import com.example.whistrentzscorer.viewmodels.RoundActions
import com.example.whistrentzscorer.viewmodels.RoundState
import com.example.whistrentzscorer.viewmodels.whistScoring
import org.junit.Assert.*
import org.junit.Test

class BiddingLogicTest {

    // ── getIllegalChoice ──

    @Test
    fun `getIllegalChoice returns null for non-last player`() {
        val roundState = mutableMapOf(
            "Alice" to RoundState(bid = 0),
            "Bob" to RoundState(bid = 1),
            "Charlie" to RoundState()
        )
        val result = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = 3,
            roundState = roundState,
            isLastPlayer = false
        )
        assertNull(result)
    }

    @Test
    fun `getIllegalChoice returns forbidden value for last player`() {
        // Alice bid 0, Bob bid 1 => total = 1, cards = 3, difference = 2
        // Last player cannot bid 2
        val roundState = mutableMapOf(
            "Alice" to RoundState(bid = 0),
            "Bob" to RoundState(bid = 1),
            "Charlie" to RoundState()
        )
        val result = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = 3,
            roundState = roundState,
            isLastPlayer = true
        )
        assertEquals(2, result)
    }

    @Test
    fun `getIllegalChoice returns 0 when other bids equal cards`() {
        // Alice bid 1, Bob bid 2 => total = 3, cards = 3, difference = 0
        val roundState = mutableMapOf(
            "Alice" to RoundState(bid = 1),
            "Bob" to RoundState(bid = 2),
            "Charlie" to RoundState()
        )
        val result = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = 3,
            roundState = roundState,
            isLastPlayer = true
        )
        assertEquals(0, result)
    }

    @Test
    fun `getIllegalChoice returns null when bids already exceed cards`() {
        // Alice bid 2, Bob bid 2 => total = 4 > 3, difference = -1
        val roundState = mutableMapOf(
            "Alice" to RoundState(bid = 2),
            "Bob" to RoundState(bid = 2),
            "Charlie" to RoundState()
        )
        val result = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = 3,
            roundState = roundState,
            isLastPlayer = true
        )
        assertNull(result)
    }

    @Test
    fun `getIllegalChoice returns null for RESULTS action`() {
        val roundState = mutableMapOf(
            "Alice" to RoundState(bid = 1),
            "Bob" to RoundState(bid = 1),
            "Charlie" to RoundState()
        )
        val result = getIllegalChoice(
            action = RoundActions.RESULTS.name,
            cardsThisRound = 3,
            roundState = roundState,
            isLastPlayer = true
        )
        assertNull(result)
    }

    // ── enabledCondition for BID ──

    @Test
    fun `bid buttons above cardsThisRound are disabled`() {
        // 3 cards in hand — buttons 4..8 should be disabled
        for (value in 4..8) {
            val enabled = enabledCondition(
                isLastPlayer = false,
                value = value,
                cardsThisRound = 3,
                handsTakenSoFar = 0,
                illegalChoice = null,
                action = RoundActions.BID.name,
                autoSelected = 0
            )
            assertFalse("Bid $value should be disabled with 3 cards", enabled)
        }
    }

    @Test
    fun `bid buttons within range are enabled`() {
        for (value in 0..3) {
            val enabled = enabledCondition(
                isLastPlayer = false,
                value = value,
                cardsThisRound = 3,
                handsTakenSoFar = 0,
                illegalChoice = null,
                action = RoundActions.BID.name,
                autoSelected = 0
            )
            assertTrue("Bid $value should be enabled with 3 cards", enabled)
        }
    }

    @Test
    fun `illegal choice button is disabled for last player bid`() {
        val enabled = enabledCondition(
            isLastPlayer = true,
            value = 2,
            cardsThisRound = 3,
            handsTakenSoFar = 0,
            illegalChoice = 2,
            action = RoundActions.BID.name,
            autoSelected = 0
        )
        assertFalse("Illegal choice 2 should be disabled", enabled)
    }

    @Test
    fun `non-illegal choices are enabled for last player bid`() {
        // illegalChoice = 2, so 0, 1, 3 should be enabled
        for (value in listOf(0, 1, 3)) {
            val enabled = enabledCondition(
                isLastPlayer = true,
                value = value,
                cardsThisRound = 3,
                handsTakenSoFar = 0,
                illegalChoice = 2,
                action = RoundActions.BID.name,
                autoSelected = 0
            )
            assertTrue("Bid $value should be enabled (illegal is 2)", enabled)
        }
    }

    @Test
    fun `bid of max cards is allowed when not illegal`() {
        // 2 cards, last player, illegal = 1 → bid 2 should be enabled
        val enabled = enabledCondition(
            isLastPlayer = true,
            value = 2,
            cardsThisRound = 2,
            handsTakenSoFar = 0,
            illegalChoice = 1,
            action = RoundActions.BID.name,
            autoSelected = 0
        )
        assertTrue("Bid 2 should be enabled with 2 cards and illegal=1", enabled)
    }

    // ── enabledCondition for RESULTS ──

    @Test
    fun `results buttons within range are all enabled for non-last player`() {
        for (value in 0..3) {
            val enabled = enabledCondition(
                isLastPlayer = false,
                value = value,
                cardsThisRound = 3,
                handsTakenSoFar = 0,
                illegalChoice = null,
                action = RoundActions.RESULTS.name,
                autoSelected = 0
            )
            assertTrue("Result $value should be enabled with 3 cards", enabled)
        }
    }

    @Test
    fun `results buttons above cardsThisRound are disabled`() {
        for (value in 4..8) {
            val enabled = enabledCondition(
                isLastPlayer = false,
                value = value,
                cardsThisRound = 3,
                handsTakenSoFar = 0,
                illegalChoice = null,
                action = RoundActions.RESULTS.name,
                autoSelected = 0
            )
            assertFalse("Result $value should be disabled with 3 cards", enabled)
        }
    }

    @Test
    fun `results buttons all enabled for last player within range`() {
        // Last player should also have all values 0..cardsThisRound enabled
        for (value in 0..3) {
            val enabled = enabledCondition(
                isLastPlayer = true,
                value = value,
                cardsThisRound = 3,
                handsTakenSoFar = 1,
                illegalChoice = null,
                action = RoundActions.RESULTS.name,
                autoSelected = 2
            )
            assertTrue("Result $value should be enabled for last player with 3 cards", enabled)
        }
    }

    // ── handsTakenSoFar ──

    @Test
    fun `handsTakenSoFar sums all players`() {
        val roundState = mutableMapOf(
            "Alice" to RoundState(handsTaken = 1),
            "Bob" to RoundState(handsTaken = 2),
            "Charlie" to RoundState(handsTaken = 0)
        )
        assertEquals(3, handsTakenSoFar(roundState))
    }

    @Test
    fun `handsTakenSoFar excludes specified player`() {
        val roundState = mutableMapOf(
            "Alice" to RoundState(handsTaken = 1),
            "Bob" to RoundState(handsTaken = 2),
            "Charlie" to RoundState(handsTaken = 0)
        )
        assertEquals(2, handsTakenSoFar(roundState, excludePlayer = "Alice"))
    }

    @Test
    fun `handsTakenSoFar treats null as 0`() {
        val roundState = mutableMapOf(
            "Alice" to RoundState(handsTaken = 1),
            "Bob" to RoundState(),
            "Charlie" to RoundState()
        )
        assertEquals(1, handsTakenSoFar(roundState))
    }

    // ── isLastPlayer ──

    @Test
    fun `isLastPlayer returns true for player before first`() {
        // 3 players, first=0. Last is player 2 because (2+1)%3 == 0
        assertTrue(isLastPlayer(currentPlayer = 2, playerCount = 3, firstPlayer = 0))
    }

    @Test
    fun `isLastPlayer returns false for non-last player`() {
        assertFalse(isLastPlayer(currentPlayer = 0, playerCount = 3, firstPlayer = 0))
        assertFalse(isLastPlayer(currentPlayer = 1, playerCount = 3, firstPlayer = 0))
    }

    @Test
    fun `isLastPlayer wraps around correctly`() {
        // 4 players, first=2. Last is player 1 because (1+1)%4 == 2
        assertTrue(isLastPlayer(currentPlayer = 1, playerCount = 4, firstPlayer = 2))
        assertFalse(isLastPlayer(currentPlayer = 3, playerCount = 4, firstPlayer = 2))
    }

    // ── whistScoring ──

    @Test
    fun `scoring success bid equals made`() {
        assertEquals(5, whistScoring(bid = 0, handsTaken = 0))
        assertEquals(6, whistScoring(bid = 1, handsTaken = 1))
        assertEquals(8, whistScoring(bid = 3, handsTaken = 3))
    }

    @Test
    fun `scoring failure bid not equals made`() {
        assertEquals(-1, whistScoring(bid = 0, handsTaken = 1))
        assertEquals(-2, whistScoring(bid = 3, handsTaken = 1))
        assertEquals(-3, whistScoring(bid = 0, handsTaken = 3))
    }
}
