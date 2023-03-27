package dev.msartore.ares.viewmodels

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.msartore.ares.ui.views.SettingsPages

class SettingsViewModel : ViewModel() {

    var onOpenThirdLicenses: (() -> Unit)? = null
    val selectedItem = mutableStateOf(SettingsPages.SETTINGS)
    val scrollState = ScrollState(0)

    fun openThirdLicenses() {
        onOpenThirdLicenses?.invoke()
    }
}