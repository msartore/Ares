package dev.msartore.ares.ui.screens.transfers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.msartore.ares.viewmodels.MainViewModel

@Composable
fun TransfersDestination(
    mainViewModel: MainViewModel,
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()

    TransferUI(
        mainViewModel = mainViewModel,
    )
}