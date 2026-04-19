package dev.msartore.ares.ui.serverFinder

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableFloatStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.msartore.ares.base.MviViewModel
import dev.msartore.ares.models.QrReadingProcess
import dev.msartore.ares.utils.cor
import javax.inject.Inject

@HiltViewModel
class ServerFinderViewModel @Inject constructor() : MviViewModel<ServerFinderState, ServerFinderEvent, ServerFinderSideEffect>(ServerFinderState()) {

    val qrReadingProcess = QrReadingProcess()
    var currentRotation by mutableFloatStateOf(0f)
    val rotation = Animatable(currentRotation)

    override suspend fun reduce(event: ServerFinderEvent) {
        when (event) {
            is ServerFinderEvent.ServerDiscovered -> {
                if (state.value.servers.none { it.ip == event.serverInfo.ip }) {
                    updateState { copy(servers = servers + event.serverInfo) }
                }
            }
            ServerFinderEvent.AllServersLost -> {
                updateState { copy(servers = emptyList()) }
            }
            is ServerFinderEvent.ServerSelected -> {
                updateState { copy(serverSelected = event.serverInfo, selectedPage = ServerFinderPages.SERVER) }
            }
            ServerFinderEvent.BackToServerList -> {
                cor {
                    kotlinx.coroutines.delay(200)
                }
                updateState { copy(serverSelected = null, selectedPage = ServerFinderPages.SERVER_LIST, serverFiles = emptyList()) }
            }
            is ServerFinderEvent.FilesLoaded -> {
                updateState { copy(serverFiles = event.files, isRefreshing = false) }
            }
            ServerFinderEvent.FilesLoadFailed -> {
                updateState { copy(error = true, isRefreshing = false) }
            }
            ServerFinderEvent.RefreshFiles -> {
                updateState { copy(isRefreshing = true) }
            }
            ServerFinderEvent.OpenQrScanner -> {
                qrReadingProcess.isReadingQR.value = true
                updateState { copy(isQrScannerActive = true) }
            }
            ServerFinderEvent.CloseQrScanner -> {
                qrReadingProcess.isReadingQR.value = false
                updateState { copy(isQrScannerActive = false) }
            }
        }
    }

    fun scanQRCode() {
        qrReadingProcess.isReadingQR.value = true
    }
}