package com.example.whistrentzscorer.navigation

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
import com.example.whistrentzscorer.components.GameSetupScreen
import com.example.whistrentzscorer.components.GamesHistory
import com.example.whistrentzscorer.components.HomeScreen
import com.example.whistrentzscorer.components.PlayersSetupScreen
import com.example.whistrentzscorer.components.RoundActionScreen
import com.example.whistrentzscorer.components.ScoreSheet
import com.example.whistrentzscorer.viewmodels.GameConfigViewModel
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import com.example.whistrentzscorer.viewmodels.HomeViewModel
import com.example.whistrentzscorer.viewmodels.RoundActions


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
        if (currRound > 1) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0)
            }
        } else {
            navController.popBackStack()
        }
    }

    val onCreateGame = {
        navController.navigate(Screen.PlayersSetup.route)
    }

    val onReviewHistory = {
        navController.navigate(Screen.GamesHistory.route)
    }

    val onPlayersAdded = {
        navController.navigate(Screen.GameSetup.route)
    }

    val onGameStarted = {
        navController.navigate(Screen.ScoreSheet.route)
    }

    val onResume = { gameId: Int? ->
        if (gameId != null) {
            navController.navigate(
                Screen.ScoreSheet.passArgs(
                    gameId.toString()
                )
            )
        }
    }

    val onRoundAction = { action: String, cards: Int ->
        navController.navigate(Screen.RoundAction.passArgs(
            action, cards
        ))
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(
            route = Screen.Home.route
        ) {
            HomeScreen(
                modifier = modifier,
                onCreateGame = { onCreateGame() },
                onReviewHistory = { onReviewHistory() },
                onResume = { onResume(gameToResume?.id) }
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
                onGameStarted = {
                    gameStateViewModel.init(sharedGameConfigViewModel.players)
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
                    // auto save game here
                    onScoreSheetBack(gameStateViewModel.currentRound)
                },
                  gameConfigViewModel = sharedGameConfigViewModel,

                onBid = { onRoundAction(RoundActions.BID.name, gameStateViewModel.currentRoundCards) },
                onInputResults = { onRoundAction(RoundActions.RESULTS.name, gameStateViewModel.currentRoundCards) },
                gameStateViewModel = gameStateViewModel
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

        //todo
        composable(
            route = Screen.GamesHistory.route
        ) {
            GamesHistory(
                onBack = { onBack() },
            )
        }
    }
}
