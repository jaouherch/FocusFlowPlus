package com.example.focusflowplus.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.focusflowplus.FocusFlowApp
import com.example.focusflowplus.ui.input.InputScreen
import com.example.focusflowplus.ui.input.InputViewModel
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object Input : Screen("input")
    object Session : Screen("session")
    object Summary : Screen("summary/{sessionId}") {
        fun createRoute(sessionId: Long) = "summary/$sessionId"
    }
    object History : Screen("history")
}

@Composable
fun FocusFlowNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as FocusFlowApp

    NavHost(
        navController = navController,
        startDestination = Screen.Input.route
    ) {
        composable(Screen.Input.route) {
            val viewModel = viewModel<InputViewModel>(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return InputViewModel(app.aiRepository) as T
                    }
                }
            )
            InputScreen(
                viewModel = viewModel,
                onStartSession = { recommendation ->
                    navController.navigate(Screen.Session.route)
                }
            )
        }
        composable(Screen.Session.route) {
            PlaceholderScreen("Session Screen — Coming Soon!")
        }
        composable(Screen.Summary.route) {
            PlaceholderScreen("Summary Screen — Coming Soon!")
        }
        composable(Screen.History.route) {
            PlaceholderScreen("History Screen — Coming Soon!")
        }
    }
}