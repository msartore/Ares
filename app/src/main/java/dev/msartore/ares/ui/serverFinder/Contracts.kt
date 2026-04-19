package dev.msartore.ares.ui.serverFinder

import dev.msartore.ares.base.UiEvent
import dev.msartore.ares.base.UiSideEffect
import dev.msartore.ares.base.UiState
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.server.ServerInfo

data class ServerFinderState(
    val selectedPage: ServerFinderPages = ServerFinderPages.SERVER_LIST,
    val serverSelected: ServerInfo? = null,
    val servers: List<ServerInfo> = emptyList(),
    val serverFiles: List<FileDataJson> = emptyList(),
    val isRefreshing: Boolean = false,
    val isQrScannerActive: Boolean = false,
    val error: Boolean = false,
) : UiState

sealed interface ServerFinderEvent : UiEvent {
    data class ServerDiscovered(val serverInfo: ServerInfo) : ServerFinderEvent
    object AllServersLost : ServerFinderEvent
    data class ServerSelected(val serverInfo: ServerInfo) : ServerFinderEvent
    object BackToServerList : ServerFinderEvent
    object RefreshFiles : ServerFinderEvent
    object OpenQrScanner : ServerFinderEvent
    object CloseQrScanner : ServerFinderEvent
    data class FilesLoaded(val files: List<FileDataJson>) : ServerFinderEvent
    object FilesLoadFailed : ServerFinderEvent
}

sealed interface ServerFinderSideEffect : UiSideEffect {
    object LaunchQrScanner : ServerFinderSideEffect
}

enum class ServerFinderPages {
    SERVER_LIST, SERVER
}