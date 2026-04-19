package dev.msartore.ares.ui.screens.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.msartore.ares.base.MviViewModel
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