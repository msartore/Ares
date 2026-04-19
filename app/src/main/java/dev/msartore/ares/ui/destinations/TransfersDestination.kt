package dev.msartore.ares.ui.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.msartore.ares.ui.views.TransferUI
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