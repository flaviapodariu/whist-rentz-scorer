package com.example.whistrentzscorer.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object PlayersSetup : Screen("playersSetup/{game}") {
        fun passArgs(game: String) = "playersSetup/$game"
    }

    data object GameSetup : Screen("gameSetup")

    data object GameSummary : Screen("summary/{playedGame}") {
        fun passArgs(playedGame: String) = "summary/$playedGame"
    }
    data object GamesHistory : Screen("history")
    data object ScoreSheet : Screen("scoresheet") {
        fun passArgs(gameId: String) = "scoresheet/$gameId"
    }
}
