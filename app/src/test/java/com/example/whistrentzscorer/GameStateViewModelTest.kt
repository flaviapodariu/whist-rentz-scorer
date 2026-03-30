package com.example.whistrentzscorer

import com.example.whistrentzscorer.components.enabledCondition
import com.example.whistrentzscorer.components.getIllegalChoice
import com.example.whistrentzscorer.components.isLastPlayer
import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.storage.repository.IGameRepository
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import com.example.whistrentzscorer.viewmodels.RoundActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameStateViewModelTest {

    private lateinit var vm: GameStateViewModel
    private val players = listOf("Alice", "Bob", "Charlie")

    @Before
    fun setup() {
        val fakeRepository = object : IGameRepository {
            override val allGames: Flow<List<GameEntity>> = flowOf(emptyList())
            override suspend fun addGame(game: Game): Long = 1L
            override suspend fun updateScore(gameId: Int, score: String) {}
            override suspend fun getGameById(gameId: Int): Game? = null
            override suspend fun getLastUnfinishedGame(): Game? = null
            override suspend fun deleteGame(gameId: Int) {}
        }
        vm = GameStateViewModel(fakeRepository)
        vm.init(players)
    }

    // ── Init ──

    @Test
    fun `init sets correct total rounds for 3 players`() {
        // 3 * 3 + 12 = 21
        assertEquals(21, vm.totalRounds)
    }

    @Test
    fun `init sets currentRound to 1`() {
        assertEquals(1, vm.currentRound)
    }

    @Test
    fun `init creates state for all rounds and players`() {
        for (round in 1..vm.totalRounds) {
            val roundState = vm.game.state[round]
            assertNotNull("Round $round should have state", roundState)
            players.forEach { player ->
                assertNotNull("Round $round should have state for $player", roundState?.get(player))
            }
        }
    }

    // ── getCurrentPlayer ──

    @Test
    fun `getCurrentPlayer returns 0 on round 1`() {
        assertEquals(0, vm.getCurrentPlayer())
    }

    @Test
    fun `getCurrentPlayer cycles through players`() {
        assertEquals(0, vm.getCurrentPlayer()) // round 1
        vm.currentRound = 2
        assertEquals(1, vm.getCurrentPlayer()) // round 2
        vm.currentRound = 3
        assertEquals(2, vm.getCurrentPlayer()) // round 3
        vm.currentRound = 4
        assertEquals(0, vm.getCurrentPlayer()) // round 4 wraps back
    }

    // ── setBid / setHandsTaken ──

    @Test
    fun `setBid stores bid correctly`() {
        vm.setBid(1, 0, 1)
        assertEquals(1, vm.getRoundStateForPlayer(1, 0).bid)
    }

    @Test
    fun `setHandsTaken stores hands correctly`() {
        vm.setHandsTaken(1, 1, 0)
        assertEquals(0, vm.getRoundStateForPlayer(1, 1).handsTaken)
    }

    // ── saveRoundScore ──

    @Test
    fun `saveRoundScore computes correct score for success`() {
        // Round 1, 1 card. first=0, last=2. All bid 0 (legal: total=0≠1). One player takes the trick.
        // Alice bids 0, takes 1 → -|0-1| = -1
        vm.setBid(1, 0, 0)
        vm.setHandsTaken(1, 0, 1)
        // Bob bids 0, takes 0 → 5 + 0 = 5
        vm.setBid(1, 1, 0)
        vm.setHandsTaken(1, 1, 0)
        // Charlie bids 0, takes 0 → 5 + 0 = 5
        vm.setBid(1, 2, 0)
        vm.setHandsTaken(1, 2, 0)

        vm.saveRoundScore(1)

        assertEquals(-1, vm.getRoundStateForPlayer(1, 0).score)
        assertEquals(5, vm.getRoundStateForPlayer(1, 1).score)
        assertEquals(5, vm.getRoundStateForPlayer(1, 2).score)
    }

    @Test
    fun `saveRoundScore computes correct score for failure`() {
        // Round 1, 1 card. All bid 0, Alice takes the trick.
        // Alice bids 0, takes 1 → -|0-1| = -1
        vm.setBid(1, 0, 0)
        vm.setHandsTaken(1, 0, 1)
        // Bob bids 0, takes 0 → 5
        vm.setBid(1, 1, 0)
        vm.setHandsTaken(1, 1, 0)
        // Charlie bids 1, takes 0 → -|1-0| = -1 (Charlie is last, illegal bid is 1-0=1, so bid 1 is illegal — but this tests scoring math, not rule enforcement)
        vm.setBid(1, 2, 1)
        vm.setHandsTaken(1, 2, 0)

        vm.saveRoundScore(1)

        assertEquals(-1, vm.getRoundStateForPlayer(1, 0).score)
        assertEquals(5, vm.getRoundStateForPlayer(1, 1).score)
        assertEquals(-1, vm.getRoundStateForPlayer(1, 2).score)
    }

    @Test
    fun `saveRoundScore accumulates across rounds`() {
        // Round 1 (first=0, last=2): all bid 0 (legal), Alice takes trick
        // Alice: bid 0 take 1 → -1
        vm.setBid(1, 0, 0); vm.setHandsTaken(1, 0, 1)
        vm.setBid(1, 1, 0); vm.setHandsTaken(1, 1, 0)
        vm.setBid(1, 2, 0); vm.setHandsTaken(1, 2, 0)
        vm.saveRoundScore(1)

        // Round 2 (first=1, last=0): all bid 0 (legal), Alice takes trick
        // Alice: bid 0 take 0 → +5, cumulative = -1 + 5 = 4
        vm.advanceRound()
        vm.setBid(2, 0, 0); vm.setHandsTaken(2, 0, 0)
        vm.setBid(2, 1, 0); vm.setHandsTaken(2, 1, 1)
        vm.setBid(2, 2, 0); vm.setHandsTaken(2, 2, 0)
        vm.saveRoundScore(2)

        assertEquals(4, vm.getRoundStateForPlayer(2, 0).score)
    }

    // ── Undo ──

    private fun completeRound(round: Int, bids: List<Int>, hands: List<Int>) {
        players.forEachIndexed { i, _ ->
            vm.setBid(round, i, bids[i])
            vm.setHandsTaken(round, i, hands[i])
        }
        vm.saveRoundScore(round)
        vm.advanceRound()
    }

    @Test
    fun `undo at round 1 does nothing`() {
        vm.undoLastTurn()
        assertEquals(1, vm.currentRound)
    }

    @Test
    fun `undo after completing round 1 and no bids on round 2 reverts to round 1`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        assertEquals(2, vm.currentRound)

        vm.undoLastTurn()

        // Should go back to round 1 and clear it
        assertEquals(1, vm.currentRound)
        assertNull(vm.getRoundStateForPlayer(1, 0).score)
        assertNull(vm.getRoundStateForPlayer(1, 0).bid)
        assertNull(vm.getRoundStateForPlayer(1, 0).handsTaken)
    }

    @Test
    fun `undo after setting bids on round 2 reverts to round 1 and clears both`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        // Set bids on round 2 but don't complete
        vm.setBid(2, 0, 0)
        vm.setBid(2, 1, 0)

        vm.undoLastTurn()

        // Should clear round 2 bids and round 1 entirely
        assertEquals(1, vm.currentRound)
        assertNull(vm.getRoundStateForPlayer(1, 0).score)
        assertNull(vm.getRoundStateForPlayer(2, 0).bid)
    }

    @Test
    fun `undo preserves correct first player after revert`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))

        vm.undoLastTurn()

        // Round 1, first player should be 0
        assertEquals(0, vm.getCurrentPlayer())
    }

    @Test
    fun `undo after two completed rounds reverts to round 2`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        assertEquals(3, vm.currentRound)

        vm.undoLastTurn()

        // No results on round 3, so clears round 3 and goes back to round 2
        assertEquals(2, vm.currentRound)
        assertNull(vm.getRoundStateForPlayer(2, 0).score)
        // Round 1 should still be intact
        assertNotNull(vm.getRoundStateForPlayer(1, 0).score)
    }

    @Test
    fun `undo first player cycles correctly for 3 players`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))
        assertEquals(4, vm.currentRound)

        vm.undoLastTurn()

        // Reverts to round 3, first player = (3-1) % 3 = 2
        assertEquals(3, vm.currentRound)
        assertEquals(2, vm.getCurrentPlayer())
    }

    @Test
    fun `double undo reverts two rounds`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))
        assertEquals(4, vm.currentRound)

        vm.undoLastTurn() // reverts to round 3
        vm.undoLastTurn() // reverts to round 2

        assertEquals(2, vm.currentRound)
        assertNull(vm.getRoundStateForPlayer(2, 0).score)
        assertNull(vm.getRoundStateForPlayer(3, 0).score)
        // Round 1 intact
        assertNotNull(vm.getRoundStateForPlayer(1, 0).score)
    }

    // ── cardsThisRound ──

    @Test
    fun `cardsThisRound returns 1 for first N rounds in 11_88_11 config`() {
        // 3 players: rounds 1-3 should have 1 card
        for (round in 1..3) {
            assertEquals("Round $round", 1, vm.cardsThisRound(round, "11..88..11"))
        }
    }

    @Test
    fun `cardsThisRound ramps up after player rounds in 11_88_11`() {
        // 3 players: round 4=2, 5=3, 6=4, 7=5, 8=6, 9=7
        for (i in 0..5) {
            assertEquals(
                "Round ${4 + i}",
                2 + i,
                vm.cardsThisRound(4 + i, "11..88..11")
            )
        }
    }

    @Test
    fun `cardsThisRound returns 8 for middle rounds in 11_88_11`() {
        // 3 players: middle rounds at 8 cards = rounds 10, 11, 12
        for (round in 10..12) {
            assertEquals("Round $round", 8, vm.cardsThisRound(round, "11..88..11"))
        }
    }

    // ── Integration: 3 players play 3 rounds then bid on 2-card round ──

    @Test
    fun `3 players complete 3 one-card rounds then round 4 has 2 cards`() {
        // Rounds 1-3: 1 card each (11..88..11 config, 3 players)
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))

        assertEquals(4, vm.currentRound)
        assertEquals(2, vm.cardsThisRound(4, "11..88..11"))
    }

    @Test
    fun `round 4 bidding allows all values 0 to 2 for first player`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))

        val round = 4
        val cardsThisRound = vm.cardsThisRound(round, "11..88..11")
        assertEquals(2, cardsThisRound)

        val firstPlayer = vm.getCurrentPlayer() // (4-1) % 3 = 0
        assertEquals(0, firstPlayer)

        val roundState = vm.game.state[round]!!
        val isLast = isLastPlayer(firstPlayer, players.size, firstPlayer)
        assertFalse("First player should not be last", isLast)

        val illegalChoice = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = cardsThisRound,
            roundState = roundState,
            isLastPlayer = isLast
        )
        assertNull("No illegal choice for first player", illegalChoice)

        // All values 0, 1, 2 should be enabled
        for (value in 0..2) {
            assertTrue(
                "Bid $value should be enabled for first player with 2 cards",
                enabledCondition(
                    isLastPlayer = isLast,
                    value = value,
                    cardsThisRound = cardsThisRound,
                    handsTakenSoFar = 0,
                    illegalChoice = illegalChoice,
                    action = RoundActions.BID.name,
                    autoSelected = 0
                )
            )
        }

        // Values 3-8 should be disabled
        for (value in 3..8) {
            assertFalse(
                "Bid $value should be disabled with 2 cards",
                enabledCondition(
                    isLastPlayer = isLast,
                    value = value,
                    cardsThisRound = cardsThisRound,
                    handsTakenSoFar = 0,
                    illegalChoice = illegalChoice,
                    action = RoundActions.BID.name,
                    autoSelected = 0
                )
            )
        }
    }

    @Test
    fun `round 4 bidding allows all values 0 to 2 for second player`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))

        val round = 4
        val cardsThisRound = 2

        // First player (Alice) bids 1
        vm.setBid(round, 0, 1)

        val secondPlayer = 1
        val firstPlayer = vm.getCurrentPlayer() // 0
        val isLast = isLastPlayer(secondPlayer, players.size, firstPlayer)
        assertFalse("Second player should not be last", isLast)

        val roundState = vm.game.state[round]!!
        val illegalChoice = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = cardsThisRound,
            roundState = roundState,
            isLastPlayer = isLast
        )
        assertNull("No illegal choice for second player", illegalChoice)

        // All values 0, 1, 2 should be enabled for second player
        for (value in 0..2) {
            assertTrue(
                "Bid $value should be enabled for second player with 2 cards",
                enabledCondition(
                    isLastPlayer = isLast,
                    value = value,
                    cardsThisRound = cardsThisRound,
                    handsTakenSoFar = 0,
                    illegalChoice = illegalChoice,
                    action = RoundActions.BID.name,
                    autoSelected = 0
                )
            )
        }
    }

    @Test
    fun `round 4 stuck rule applies to last player`() {
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))

        val round = 4
        val cardsThisRound = 2
        val firstPlayer = vm.getCurrentPlayer() // 0

        // Alice bids 1, Bob bids 0 => total = 1, cards = 2, illegal = 1 for last player
        vm.setBid(round, 0, 1)
        vm.setBid(round, 1, 0)

        val lastPlayerIndex = 2 // Charlie
        val isLast = isLastPlayer(lastPlayerIndex, players.size, firstPlayer)
        assertTrue("Third player should be last", isLast)

        val roundState = vm.game.state[round]!!
        val illegalChoice = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = cardsThisRound,
            roundState = roundState,
            isLastPlayer = isLast
        )
        assertEquals("Illegal choice should be 1 (2 - 1)", 1, illegalChoice)

        // Value 1 should be disabled (stuck rule)
        assertFalse(
            "Bid 1 should be disabled for last player (stuck rule)",
            enabledCondition(
                isLastPlayer = isLast,
                value = 1,
                cardsThisRound = cardsThisRound,
                handsTakenSoFar = 0,
                illegalChoice = illegalChoice,
                action = RoundActions.BID.name,
                autoSelected = 0
            )
        )

        // Values 0 and 2 should be enabled
        for (value in listOf(0, 2)) {
            assertTrue(
                "Bid $value should be enabled for last player (illegal is 1)",
                enabledCondition(
                    isLastPlayer = isLast,
                    value = value,
                    cardsThisRound = cardsThisRound,
                    handsTakenSoFar = 0,
                    illegalChoice = illegalChoice,
                    action = RoundActions.BID.name,
                    autoSelected = 0
                )
            )
        }

    }

    @Test
    fun `round 4 currentRoundCards must be 2 not 1`() {
        // This test reproduces the actual bug: the app passes currentRoundCards
        // to the bid screen, but currentRoundCards is never updated when
        // currentRound changes — it stays at 1 forever.
        completeRound(1, listOf(0, 0, 0), listOf(0, 0, 1))
        completeRound(2, listOf(0, 0, 0), listOf(1, 0, 0))
        completeRound(3, listOf(0, 0, 0), listOf(0, 1, 0))

        assertEquals(4, vm.currentRound)

        // This is what the UI actually uses — must be 2, not 1
        assertEquals(
            "currentRoundCards must reflect round 4 (2 cards), not stay at init value (1)",
            2,
            vm.currentRoundCards
        )

        // Use currentRoundCards (same as the real UI) to check if bid 2 is enabled
        val cardsThisRound = vm.currentRoundCards
        val firstPlayer = vm.getCurrentPlayer()
        val isLast = isLastPlayer(firstPlayer, players.size, firstPlayer)

        val roundState = vm.game.state[4]!!
        val illegalChoice = getIllegalChoice(
            action = RoundActions.BID.name,
            cardsThisRound = cardsThisRound,
            roundState = roundState,
            isLastPlayer = isLast
        )

        assertTrue(
            "Bid 2 must be enabled on a 2-card round for first player",
            enabledCondition(
                isLastPlayer = isLast,
                value = 2,
                cardsThisRound = cardsThisRound,
                handsTakenSoFar = 0,
                illegalChoice = illegalChoice,
                action = RoundActions.BID.name,
                autoSelected = 0
            )
        )
    }
}
