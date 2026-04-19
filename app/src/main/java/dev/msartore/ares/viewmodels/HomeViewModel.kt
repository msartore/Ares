package dev.msartore.ares.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.msartore.ares.base.MviViewModel
import dev.msartore.ares.ui.destinations.HomeEvent
import dev.msartore.ares.ui.destinations.HomeSideEffect
import dev.msartore.ares.ui.destinations.HomeState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : MviViewModel<HomeState, HomeEvent, HomeSideEffect>(HomeState()) {

    override suspend fun reduce(event: HomeEvent) {
        when (event) {
            is HomeEvent.ImportFilesClicked -> emitSideEffect(HomeSideEffect.LaunchFilePicker)
            is HomeEvent.StartServerClicked -> emitSideEffect(HomeSideEffect.StartKtorService)
            is HomeEvent.StopServerClicked -> emitSideEffect(HomeSideEffect.StopKtorService)
            is HomeEvent.ShowInputDialog -> updateState { copy(inputDialogVisible = true) }
            is HomeEvent.DismissInputDialog -> updateState { copy(inputDialogVisible = false, inputText = "") }
            is HomeEvent.InputTextChanged -> updateState { copy(inputText = event.text) }
            is HomeEvent.ServerStateChanged -> updateState { copy(isServerRunning = event.isRunning) }
            is HomeEvent.LoadingChanged -> updateState { copy(isLoading = event.isLoading) }
        }
    }
}