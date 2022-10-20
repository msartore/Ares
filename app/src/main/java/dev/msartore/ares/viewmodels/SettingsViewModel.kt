package dev.msartore.ares.viewmodels

import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    var onOpenThirdLicenses: (() -> Unit)? = null

    fun openThirdLicenses() {
        onOpenThirdLicenses?.invoke()
    }
}