package dev.msartore.ares.ui.screens.settings

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.msartore.ares.ui.navigation.MainRoutes
import dev.msartore.ares.ui.screens.main.MainViewModel

fun NavGraphBuilder.settingsDestination(
    settings: dev.msartore.ares.models.Settings?,
    mainViewModel: MainViewModel,
    onLaunchOssLicenses: () -> Unit,
) {
    composable(MainRoutes.SETTINGS.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    SettingsSideEffect.LaunchOssLicensesActivity -> onLaunchOssLicenses()
                }
            }
        }

        SettingsScreen(
            state = state,
            onEvent = viewModel::onEvent,
            settings = settings,
            mainViewModel = mainViewModel,
        )
    }
}