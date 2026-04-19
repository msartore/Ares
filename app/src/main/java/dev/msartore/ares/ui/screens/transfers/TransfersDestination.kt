package dev.msartore.ares.ui.screens.transfers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.msartore.ares.ui.screens.main.MainViewModel

@Composable
fun TransfersDestination(
    mainViewModel: MainViewModel,
) {

    TransferUI(
        mainViewModel = mainViewModel,
    )
}