package com.example.whistrentzscorer

import com.example.whistrentzscorer.storage.repository.IGameRepository
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class GameStateViewModelTest {

    private lateinit var vm: GameStateViewModel
    private val players = listOf("Alice", "Bob", "Charlie")

    @Before
    fun setup() {
        val fakeRepository: IGameRepository = mock()
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
}