package dev.msartore.ares.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel : ViewModel() {

    val isLoading = MutableStateFlow(false)
    var onImportFiles: (() -> Unit)? = null
    var onStartServer: (() -> Unit)? = null
    var onStopServer: (() -> Unit)? = null

    fun onImportFilesClick() {
        onImportFiles?.invoke()
    }

    fun onStartServerClick() {
        onStartServer?.invoke()
    }

    fun onStopServerClick() {
        onStopServer?.invoke()
    }
}