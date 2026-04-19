package dev.msartore.ares.ui.destinations

import dev.msartore.ares.base.UiEvent
import dev.msartore.ares.base.UiSideEffect
import dev.msartore.ares.base.UiState
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDownload
import dev.msartore.ares.models.FileType
import dev.msartore.ares.models.TransferFile
import dev.msartore.ares.utils.BackgroundPStatus

data class MainState(
    val transferredFiles: List<TransferFile> = emptyList(),
    val listFileDownload: List<FileDownload> = emptyList(),
    val qrCodeDialog: Boolean = false,
    val backgroundPStatus: BackgroundPStatus? = null,
) : UiState

sealed interface MainEvent : UiEvent {
    data class FileDownloadCompleted(val fileData: FileData?) : MainEvent
    data class FileUploadCompleted(val fileData: FileData?) : MainEvent
    data class OpenFileDownload(val fileDownload: FileDownload) : MainEvent
    data class ShareFileDownload(val fileDownload: FileDownload) : MainEvent
    data class DismissFileDownload(val fileDownload: FileDownload) : MainEvent
    data class CopyTextRequested(val label: String, val text: String) : MainEvent
    data class ShareTextRequested(val text: String) : MainEvent
    data class OpenFileRequested(val fileData: FileData) : MainEvent
    data class ShareFileRequested(val fileData: FileData) : MainEvent
    data class OpenStreamingRequested(val url: String, val fileType: FileType?) : MainEvent
    data class UrlOpened(val url: String) : MainEvent
    object BackgroundClick : MainEvent
    object DismissQrCodeDialog : MainEvent
}

sealed interface MainSideEffect : UiSideEffect {
    data class CopyToClipboard(val label: String, val text: String) : MainSideEffect
    data class ShareText(val text: String) : MainSideEffect
    data class OpenFile(val fileData: FileData) : MainSideEffect
    data class ShareFile(val fileData: FileData) : MainSideEffect
    data class OpenUrl(val url: String) : MainSideEffect
    data class LaunchStreamingIntent(val url: String, val mimeType: String) : MainSideEffect
    data class ShowToast(val messageRes: Int) : MainSideEffect
}