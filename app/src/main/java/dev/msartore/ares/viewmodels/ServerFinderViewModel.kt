package dev.msartore.ares.viewmodels

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.msartore.ares.base.MviViewModel
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.models.QrReadingProcess
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.destinations.ServerFinderEvent
import dev.msartore.ares.ui.destinations.ServerFinderPages
import dev.msartore.ares.ui.destinations.ServerFinderSideEffect
import dev.msartore.ares.ui.destinations.ServerFinderState
import dev.msartore.ares.ui.views.ServerFinderPages as UiPages
import dev.msartore.ares.utils.cor
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class ServerFinderViewModel @Inject constructor() : MviViewModel<ServerFinderState, ServerFinderEvent, ServerFinderSideEffect>(ServerFinderState()) {

    private val servers = mutableStateListOf<ServerInfo>()
    val qrReadingProcess = QrReadingProcess()
    val selectedItem = mutableStateOf(UiPages.SERVER_LIST)
    val serverSelected = mutableStateOf<ServerInfo?>(null)
    var job: Job? = null
    var isNewServer = false
    val serverFiles = mutableStateListOf<FileDataJson>()
    val error = mutableStateOf(false)
    var currentRotation by mutableFloatStateOf(0f)
    val rotation = Animatable(currentRotation)
    val isRefreshing = mutableStateOf(false)
    var gridState: LazyGridState = LazyGridState()
    var scrollState: ScrollState = ScrollState(0)
    val serversCount = mutableIntStateOf(0)

    override suspend fun reduce(event: ServerFinderEvent) {
        when (event) {
            is ServerFinderEvent.ServerDiscovered -> {
                if (servers.none { it.ip == event.serverInfo.ip }) {
                    servers.add(event.serverInfo)
                    serversCount.intValue = servers.size
                }
                updateState { copy(servers = servers.toList()) }
            }
            ServerFinderEvent.AllServersLost -> {
                servers.clear()
                serversCount.intValue = 0
                updateState { copy(servers = emptyList()) }
            }
            is ServerFinderEvent.ServerSelected -> {
                selectedItem.value = UiPages.SERVER
                serverSelected.value = event.serverInfo
                isNewServer = true
                updateState { copy(serverSelected = event.serverInfo, selectedPage = ServerFinderPages.SERVER) }
            }
            ServerFinderEvent.BackToServerList -> {
                cor {
                    job?.cancel()
                    selectedItem.value = UiPages.SERVER_LIST
                    kotlinx.coroutines.delay(200)
                    serverSelected.value = null
                    isRefreshing.value = false
                    scrollState = ScrollState(0)
                    error.value = false
                    serverFiles.clear()
                    gridState = LazyGridState()
                }
                updateState { copy(serverSelected = null, selectedPage = ServerFinderPages.SERVER_LIST, serverFiles = emptyList()) }
            }
            is ServerFinderEvent.FilesLoaded -> {
                serverFiles.addAll(event.files)
                updateState { copy(serverFiles = event.files, isRefreshing = false) }
            }
            ServerFinderEvent.FilesLoadFailed -> {
                error.value = true
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

    fun setServer(serverInfo: ServerInfo) {
        selectedItem.value = UiPages.SERVER
        serverSelected.value = serverInfo
        isNewServer = true
    }

    fun backToScanWifi() {
        cor {
            job?.cancel()
            selectedItem.value = UiPages.SERVER_LIST
            kotlinx.coroutines.delay(200)
            serverSelected.value = null
            isRefreshing.value = false
            scrollState = ScrollState(0)
            error.value = false
            serverFiles.clear()
            gridState = LazyGridState()
        }
    }

    fun addServer(serverInfo: ServerInfo) {
        if (servers.none { it.ip == serverInfo.ip }) {
            servers.add(serverInfo)
            serversCount.intValue = servers.size
        }
    }

    fun clearServers() {
        servers.clear()
        serversCount.intValue = 0
    }

    fun getServers() = servers
}