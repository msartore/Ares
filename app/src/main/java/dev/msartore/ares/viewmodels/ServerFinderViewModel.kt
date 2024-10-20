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
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.models.QrReadingProcess
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.views.ServerFinderPages
import dev.msartore.ares.utils.cor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class ServerFinderViewModel : ViewModel() {

    private val servers = mutableStateListOf<ServerInfo>()

    val qrReadingProcess = QrReadingProcess()
    val selectedItem = mutableStateOf(ServerFinderPages.SERVER_LIST)
    val serverSelected = mutableStateOf<ServerInfo?>(null)
    var job: Job? = null
    var isNewServer = false
    val serverFiles = mutableStateListOf<FileDataJson>()
    val error = mutableStateOf(false)
    var currentRotation by mutableFloatStateOf(0f)
    val rotation = Animatable(currentRotation)
    val isRefreshing = mutableStateOf(false)
    var state: LazyGridState = LazyGridState()
    var scrollState: ScrollState = ScrollState(0)
    val serversCount = mutableIntStateOf(0)

    fun scanQRCode() {
        qrReadingProcess.isReadingQR.value = true
    }

    fun setServer(serverInfo: ServerInfo) {
        selectedItem.value = ServerFinderPages.SERVER
        serverSelected.value = serverInfo
        isNewServer = true
    }

    fun backToScanWifi() {
        cor {
            job?.cancel()
            selectedItem.value = ServerFinderPages.SERVER_LIST
            delay(200)
            serverSelected.value = null
            isRefreshing.value = false
            scrollState = ScrollState(0)
            error.value = false
            serverFiles.clear()
            state = LazyGridState()
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