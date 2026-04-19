package dev.msartore.ares.ui.destinations

import dev.msartore.ares.base.UiEvent
import dev.msartore.ares.base.UiSideEffect
import dev.msartore.ares.base.UiState

enum class SettingsPages {
    SETTINGS, ABOUT
}

data class SettingsState(
    val selectedPage: SettingsPages = SettingsPages.SETTINGS,
) : UiState

sealed interface SettingsEvent : UiEvent {
    object OpenThirdLicensesClicked : SettingsEvent
    object BackClicked : SettingsEvent
    data class NavigateTo(val page: SettingsPages) : SettingsEvent
}

sealed interface SettingsSideEffect : UiSideEffect {
    object LaunchOssLicensesActivity : SettingsSideEffect
}