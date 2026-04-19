package dev.msartore.ares.ui.destinations

import dev.msartore.ares.base.UiEvent
import dev.msartore.ares.base.UiSideEffect
import dev.msartore.ares.base.UiState

data class HomeState(
    val isLoading: Boolean = false,
    val isServerRunning: Boolean = false,
    val inputDialogVisible: Boolean = false,
    val inputText: String = "",
) : UiState

sealed interface HomeEvent : UiEvent {
    object ImportFilesClicked : HomeEvent
    object StartServerClicked : HomeEvent
    object StopServerClicked : HomeEvent
    object ShowInputDialog : HomeEvent
    object DismissInputDialog : HomeEvent
    data class InputTextChanged(val text: String) : HomeEvent
    data class ServerStateChanged(val isRunning: Boolean) : HomeEvent
    data class LoadingChanged(val isLoading: Boolean) : HomeEvent
}

sealed interface HomeSideEffect : UiSideEffect {
    object LaunchFilePicker : HomeSideEffect
    object StartKtorService : HomeSideEffect
    object StopKtorService : HomeSideEffect
}