package com.example.focusflowplus.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.focusflowplus.FocusFlowApp
import com.example.focusflowplus.ui.history.HistoryScreen
import com.example.focusflowplus.ui.history.HistoryViewModel
import com.example.focusflowplus.ui.input.InputScreen
import com.example.focusflowplus.ui.input.InputViewModel
import com.example.focusflowplus.ui.session.SessionScreen
import com.example.focusflowplus.ui.session.SessionViewModel
import com.example.focusflowplus.ui.summary.SummaryScreen
import com.example.focusflowplus.ui.summary.SummaryViewModel

sealed class Screen(val route: String) {
    object Input : Screen("input")
    object Session : Screen("session/{sessionId}") {
        fun createRoute(sessionId: Long) = "session/$sessionId"
    }
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
                factory = simpleFactory {
                    InputViewModel(app.aiRepository, app.sessionRepository)
                }
            )
            InputScreen(
                viewModel = viewModel,
                onStartSession = { sessionId ->
                    navController.navigate(Screen.Session.createRoute(sessionId))
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(
            route = Screen.Session.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val viewModel = viewModel<SessionViewModel>(
                key = "session-$sessionId",
                factory = simpleFactory {
                    SessionViewModel(sessionId, app.sessionRepository)
                }
            )
            SessionScreen(
                viewModel = viewModel,
                onBack = {
                    navController.navigate(Screen.Input.route) {
                        popUpTo(Screen.Input.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onFinish = { finishedSessionId ->
                    navController.navigate(Screen.Summary.createRoute(finishedSessionId)) {
                        popUpTo(Screen.Session.createRoute(finishedSessionId)) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Summary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val viewModel = viewModel<SummaryViewModel>(
                key = "summary-$sessionId",
                factory = simpleFactory {
                    SummaryViewModel(sessionId, app.sessionRepository)
                }
            )
            SummaryScreen(
                viewModel = viewModel,
                onNewSession = {
                    navController.navigate(Screen.Input.route) {
                        popUpTo(Screen.Input.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.History.route) {
            val viewModel = viewModel<HistoryViewModel>(
                factory = simpleFactory {
                    HistoryViewModel(app.sessionRepository)
                }
            )
            HistoryScreen(
                viewModel = viewModel,
                onBack = {
                    navController.navigate(Screen.Input.route) {
                        popUpTo(Screen.Input.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.Session.createRoute(sessionId))
                }
            )
        }
    }
}

private inline fun <reified T : ViewModel> simpleFactory(crossinline creator: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return creator() as VM
        }
    }
}
