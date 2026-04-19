package dev.msartore.ares.ui.screens.transfers

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.msartore.ares.ui.navigation.MainRoutes
import dev.msartore.ares.ui.screens.main.MainViewModel

fun NavGraphBuilder.transfersDestination(
    mainViewModel: MainViewModel,
) {
    composable(MainRoutes.TRANSFERS.route) {
        TransferUI(
            mainViewModel = mainViewModel,
        )
    }
}