package com.example.whistrentzscorer

import com.example.whistrentzscorer.storage.repository.IGameRepository
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock

class CardsThisRoundTest {

    companion object {
        @JvmStatic
        fun playerCount() = listOf(3, 4, 5, 6)

        private fun playerNames(n: Int): List<String> =
            (1..n).map { "P$it" }
    }

    private val repository: IGameRepository = mock()

    private fun createVM(playerCount: Int): GameStateViewModel {
        val vm = GameStateViewModel(repository)
        vm.init(playerNames(playerCount), "11..88..11")
        return vm
    }

    // Round structure for N players, "11..88..11":
    // Rounds  1..N:          1 card  (start)
    // Rounds  N+1..N+6:      2,3,4,5,6,7 (up)
    // Rounds  N+7..2N+6:     8 cards (mid)
    // Rounds  2N+7..2N+12:   7,6,5,4,3,2 (down)
    // Rounds  2N+13..3N+12:  1 card  (end)
    // Total = N*3 + 12

    // ── "11..88..11" ──

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `total rounds count is correct`(playerCount: Int) {
        val vm = createVM(playerCount)
        assertEquals(playerCount * 3 + 12, vm.totalRounds)
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `starting rounds return 1 card`(n: Int) {
        val vm = createVM(n)
        for (round in 1..n) {
            assertEquals(1, vm.cardsThisRound(round, "11..88..11", n), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `up phase ramps from 2 to 7`(n: Int) {
        val vm = createVM(n)
        val expected = listOf(2, 3, 4, 5, 6, 7)
        for (i in expected.indices) {
            val round = n + 1 + i
            assertEquals(expected[i], vm.cardsThisRound(round, "11..88..11", n), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `middle rounds return 8 cards`(n: Int) {
        val vm = createVM(n)
        for (round in (n + 7)..(2 * n + 6)) {
            assertEquals(8, vm.cardsThisRound(round, "11..88..11", n), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `down phase ramps from 7 to 2`(playerCount: Int) {
        val vm = createVM(playerCount)
        val expected = listOf(7, 6, 5, 4, 3, 2)
        for (i in expected.indices) {
            val round = 2 * playerCount + 7 + i
            assertEquals(expected[i],
                vm.cardsThisRound(round, "11..88..11", playerCount),
                "Round $round"
            )
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `ending rounds return 1 card`(playerCount: Int) {
        val vm = createVM(playerCount)
        for (round in (2 * playerCount + 13)..(3 * playerCount + 12)) {
            assertEquals(1, vm.cardsThisRound(round, "11..88..11", playerCount), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `out of bounds round returns 0`(playerCount: Int) {
        val vm = createVM(playerCount)
        val totalRounds = playerCount * 3 + 12
        assertEquals(0, vm.cardsThisRound(totalRounds + 1, "11..88..11", playerCount))
    }

    // ── "88..11..88" (reversed) ──

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `reversed - starting rounds return 8 cards`(playerCount: Int) {
        val vm = createVM(playerCount)
        for (round in 1..playerCount) {
            assertEquals(8, vm.cardsThisRound(round, "88..11..88", playerCount), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `reversed - up phase ramps from 7 to 2`(playerCount: Int) {
        val vm = createVM(playerCount)
        val expected = listOf(7, 6, 5, 4, 3, 2)
        for (i in expected.indices) {
            val round = playerCount + 1 + i
            assertEquals(expected[i], vm.cardsThisRound(round, "88..11..88", playerCount), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `reversed - middle rounds return 1 card`(playerCount: Int) {
        val vm = createVM(playerCount)
        for (round in (playerCount + 7)..(2 * playerCount + 6)) {
            assertEquals(1, vm.cardsThisRound(round, "88..11..88", playerCount), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `reversed - down phase ramps from 2 to 7`(playerCount: Int) {
        val vm = createVM(playerCount)
        val expected = listOf(2, 3, 4, 5, 6, 7)
        for (i in expected.indices) {
            val round = 2 * playerCount + 7 + i
            assertEquals(expected[i], vm.cardsThisRound(round, "88..11..88", playerCount), "Round $round")
        }
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `reversed - ending rounds return 8 cards`(playerCount: Int) {
        val vm = createVM(playerCount)
        for (round in (2 * playerCount + 13)..(3 * playerCount + 12)) {
            assertEquals(8, vm.cardsThisRound(round, "88..11..88", playerCount), "Round $round")
        }
    }

    // ── currentRoundCards stays in sync after advanceRound ──

    private fun completeRound(vm: GameStateViewModel, round: Int) {
        vm.playerList.forEachIndexed { i, _ ->
            vm.setBid(round, i, 0)
            vm.setHandsTaken(round, i, if (i == 0) 1 else 0)
        }
        vm.saveRoundScore(round)
        vm.advanceRound()
    }

    @ParameterizedTest(name = "{0} players")
    @MethodSource("playerCount")
    fun `currentRoundCards updates after advancing past start phase`(playerCount: Int) {
        val vm = createVM(playerCount)
        assertEquals(1, vm.currentRoundCards)

        for (round in 1..playerCount) {
            completeRound(vm, round)
        }

        assertEquals(playerCount + 1, vm.currentRound)
        assertEquals(2, vm.currentRoundCards)
    }
}
