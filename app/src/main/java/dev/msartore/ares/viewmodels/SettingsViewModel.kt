package dev.msartore.ares.viewmodels

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
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

    @Suppress("DEPRECATION")
    fun packageInfo(context: Context): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0L))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
}