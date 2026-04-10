package com.example.whistrentzscorer.navigation

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.whistrentzscorer.components.rentz.CountPerPlayerScreen
import com.example.whistrentzscorer.components.rentz.MiniGameSelectionScreen
import com.example.whistrentzscorer.components.rentz.RentzRankScreen
import com.example.whistrentzscorer.components.rentz.RentzWhistScreen
import com.example.whistrentzscorer.components.rentz.SinglePlayerCheckboxScreen
import com.example.whistrentzscorer.components.rentz.TotaleScreen
import com.example.whistrentzscorer.components.shared.GamesHistory
import com.example.whistrentzscorer.components.shared.HomeScreenV2
import com.example.whistrentzscorer.components.whist.GameSetupScreen
import com.example.whistrentzscorer.components.whist.PlayersSetupScreen
import com.example.whistrentzscorer.components.whist.RoundActionScreen
import com.example.whistrentzscorer.components.whist.ScoreSheet
import com.example.whistrentzscorer.objects.RentzInputType
import com.example.whistrentzscorer.objects.RentzMiniGame
import com.example.whistrentzscorer.viewmodels.whist.GameConfigViewModel
import com.example.whistrentzscorer.viewmodels.whist.state.GameStateViewModel
import com.example.whistrentzscorer.viewmodels.HomeViewModel
import com.example.whistrentzscorer.viewmodels.whist.state.RoundActions


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    activity: ComponentActivity,
    modifier: Modifier,
) {
    val sharedGameConfigViewModel: GameConfigViewModel = hiltViewModel(activity)
    val homeViewModel: HomeViewModel = hiltViewModel(activity)
    val gameStateViewModel: GameStateViewModel = hiltViewModel(activity)

    val gameToResume by homeViewModel.gameToResume.collectAsState(initial = null)

    val onBack = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }

    val onScoreSheetBack = { currRound: Int ->
        homeViewModel.loadLastUnfinishedGame()
        if (currRound > 1) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0)
            }
        } else {
            navController.popBackStack()
        }
    }

    val onCreateGame = {
        sharedGameConfigViewModel.gameMode = homeViewModel.selectedGameMode.value
        navController.navigate(Screen.PlayersSetup.route)
    }

    val onReviewHistory = {
        navController.navigate(Screen.GamesHistory.route)
    }

    val onPlayersAdded = {
        navController.navigate(Screen.GameSetup.route)
    }

    val onGameStarted = {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        navController.navigate(Screen.ScoreSheet.route)
    }

    val onResume = {
        val savedGame = gameToResume
        if (savedGame != null) {
            gameStateViewModel.restoreGame(
                id = savedGame.id,
                players = savedGame.players,
                scoresJson = savedGame.scoresJson,
                gameMode = savedGame.gameMode,
                elapsedTime = savedGame.elapsedTime
            )
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            navController.navigate(Screen.ScoreSheet.route)
        }
    }

    val onRoundAction = { action: String, cards: Int ->
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        navController.navigate(Screen.RoundAction.passArgs(
            action, cards
        ))
    }

    val onSelectMiniGame = {
        navController.navigate(Screen.MiniGameSelection.route)
    }

    val onMiniGameSelected = { miniGame: RentzMiniGame ->
        gameStateViewModel.selectMiniGame(miniGame)
        navController.navigate(Screen.RentzResult.passArgs(miniGame.name))
    }

    val onRentzResultSubmit = { scores: Map<String, Int> ->
        gameStateViewModel.submitRentzRoundScores(scores)
        // Pop back to ScoreSheet
        navController.popBackStack(Screen.ScoreSheet.route, inclusive = false)
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(
            route = Screen.Home.route
        ) {
            HomeScreenV2(
                modifier = modifier,
                onCreateGame = { onCreateGame() },
                onReviewHistory = { onReviewHistory() },
                onResume = { onResume() },
                viewModel = homeViewModel
            )
        }

        composable(
            route = Screen.PlayersSetup.route
        ) {
            PlayersSetupScreen(
                onPlayersAdded = { onPlayersAdded() },
                onBack = { onBack() },
                viewModel = sharedGameConfigViewModel
            )
        }

        composable(
            route = Screen.GameSetup.route
        ) {
            GameSetupScreen(
                onGameStarted = { gameId ->
                    gameStateViewModel.init(
                        sharedGameConfigViewModel.players,
                        sharedGameConfigViewModel.gameType,
                        sharedGameConfigViewModel.gameMode,
                        sharedGameConfigViewModel.bonus
                    )
                    gameStateViewModel.setGameId(gameId.toInt())
                    onGameStarted() },
                onBack = { onBack() },
                viewModel = sharedGameConfigViewModel
            )
        }

        composable(
            route = Screen.ScoreSheet.route
        ) {
            ScoreSheet(
                onBack = {
                    gameStateViewModel.autoSave()
                    onScoreSheetBack(gameStateViewModel.currentRound)
                },
                onBid = { onRoundAction(RoundActions.BID.name, gameStateViewModel.currentRoundCards) },
                onInputResults = { onRoundAction(RoundActions.RESULTS.name, gameStateViewModel.currentRoundCards) },
                stateVM = gameStateViewModel,
                isRentz = gameStateViewModel.isRentzGame(),
                onSelectMiniGame = { onSelectMiniGame() }
            )
        }

        composable(
            route = Screen.RoundAction.route,
            arguments = listOf(
                navArgument("action") { type = NavType.StringType },
                navArgument("cards") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString("action") ?: ""
            val cards = backStackEntry.arguments?.getInt("cards") ?: 1

            RoundActionScreen(
                cardsThisRound = cards,
                action = action,
                onBack = { onBack() },
                gameStateViewModel = gameStateViewModel
            )
        }

        composable(
            route = Screen.MiniGameSelection.route
        ) {
            MiniGameSelectionScreen(
                onMiniGameSelected = { miniGame -> onMiniGameSelected(miniGame) },
                onBack = { onBack() },
                playedGames = gameStateViewModel.playedMiniGames.toSet()
            )
        }

        composable(
            route = Screen.RentzResult.route,
            arguments = listOf(
                navArgument("miniGame") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val miniGameName = backStackEntry.arguments?.getString("miniGame") ?: ""
            val miniGame = try { RentzMiniGame.valueOf(miniGameName) } catch (e: Exception) { null }
            val players = gameStateViewModel.playerList

            if (miniGame != null) {
                when (miniGame.inputType) {
                    RentzInputType.SINGLE_PLAYER_CHECKBOX -> {
                        SinglePlayerCheckboxScreen(
                            miniGame = miniGame,
                            players = players,
                            onSubmit = { scores -> onRentzResultSubmit(scores) },
                            onBack = { onBack() }
                        )
                    }
                    RentzInputType.COUNT_PER_PLAYER -> {
                        CountPerPlayerScreen(
                            miniGame = miniGame,
                            players = players,
                            onSubmit = { scores -> onRentzResultSubmit(scores) },
                            onBack = { onBack() }
                        )
                    }
                    RentzInputType.WHIST -> {
                        RentzWhistScreen(
                            players = players,
                            onSubmit = { scores -> onRentzResultSubmit(scores) },
                            onBack = { onBack() }
                        )
                    }
                    RentzInputType.RENTZ -> {
                        RentzRankScreen(
                            players = players,
                            onSubmit = { scores -> onRentzResultSubmit(scores) },
                            onBack = { onBack() }
                        )
                    }
                    RentzInputType.TOTALE -> {
                        TotaleScreen(
                            players = players,
                            onSubmit = { scores -> onRentzResultSubmit(scores) },
                            onBack = { onBack() }
                        )
                    }
                }
            }
        }

        composable(
            route = Screen.GamesHistory.route
        ) {
            GamesHistory(
                onBack = { onBack() },
                onGameClick = { game ->
                    val players = game.parsePlayers()
                    gameStateViewModel.restoreGame(
                        id = game.id,
                        players = players,
                        scoresJson = game.scoresJson,
                        gameMode = game.gameMode,
                        elapsedTime = game.elapsedTime
                    )
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    navController.navigate(Screen.ScoreSheet.route)
                },
                viewModel = homeViewModel
            )
        }
    }
}
