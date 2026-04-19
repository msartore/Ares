package dev.msartore.ares.ui.screens.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.ui.navigation.MainRoutes
import dev.msartore.ares.ui.screens.main.MainViewModel

fun NavGraphBuilder.homeDestination(
    settings: dev.msartore.ares.models.Settings?,
    mainViewModel: MainViewModel,
    onLaunchFilePicker: () -> Unit,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    onBackgroundClick: () -> Unit,
    maxWidth: Dp = 0.dp,
) {
    composable(MainRoutes.HOME.route) {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(KtorService.KtorServer.isServerOn) {
            viewModel.onEvent(HomeEvent.ServerStateChanged(KtorService.KtorServer.isServerOn.value))
        }

        LaunchedEffect(Unit) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    HomeSideEffect.LaunchFilePicker -> onLaunchFilePicker()
                    HomeSideEffect.StartKtorService -> onStartServer()
                    HomeSideEffect.StopKtorService -> onStopServer()
                }
            }
        }

        HomeScreen(
            state = state,
            onEvent = viewModel::onEvent,
            settings = settings,
            mainViewModel = mainViewModel,
            onBackgroundClick = onBackgroundClick,
            maxWidth = maxWidth,
        )
    }
}