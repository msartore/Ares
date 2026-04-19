package dev.msartore.ares.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.msartore.ares.viewmodels.MainViewModel

@Composable
fun SettingsDestination(
    viewModel: SettingsViewModel = hiltViewModel(),
    settings: dev.msartore.ares.models.Settings?,
    mainViewModel: MainViewModel,
    onLaunchOssLicenses: () -> Unit,
) {
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