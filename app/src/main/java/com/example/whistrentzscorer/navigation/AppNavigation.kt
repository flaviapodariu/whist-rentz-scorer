package com.example.whistrentzscorer.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whistrentzscorer.components.GameSetupScreen
import com.example.whistrentzscorer.components.GamesHistory
import com.example.whistrentzscorer.components.HomeScreen
import com.example.whistrentzscorer.components.PlayersSetupScreen
import com.example.whistrentzscorer.components.ScoreSheet
import com.example.whistrentzscorer.viewmodels.GameConfigViewModel
import com.example.whistrentzscorer.viewmodels.HomeViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    activity: ComponentActivity,
    modifier: Modifier,
) {

    val sharedGameConfigViewModel: GameConfigViewModel = hiltViewModel(activity)
    val homeViewModel: HomeViewModel = hiltViewModel(activity)
    val gameToResume by homeViewModel.gameToResume.collectAsState(initial = null)

    val onBack = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }
    val goToHome = {
        navController.navigate(Screen.Home.route)
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
                onGameStarted = { onGameStarted() },
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
                    goToHome()
                },
                gameConfigViewModel = sharedGameConfigViewModel
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
