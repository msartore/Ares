package dev.msartore.ares.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.msartore.ares.base.MviViewModel
import dev.msartore.ares.ui.destinations.SettingsEvent
import dev.msartore.ares.ui.destinations.SettingsPages
import dev.msartore.ares.ui.destinations.SettingsSideEffect
import dev.msartore.ares.ui.destinations.SettingsState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : MviViewModel<SettingsState, SettingsEvent, SettingsSideEffect>(SettingsState()) {

    override suspend fun reduce(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OpenThirdLicensesClicked -> emitSideEffect(SettingsSideEffect.LaunchOssLicensesActivity)
            is SettingsEvent.BackClicked -> updateState { copy(selectedPage = SettingsPages.SETTINGS) }
            is SettingsEvent.NavigateTo -> updateState { copy(selectedPage = event.page) }
        }
    }
}