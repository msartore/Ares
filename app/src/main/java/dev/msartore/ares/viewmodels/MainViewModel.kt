package dev.msartore.ares.viewmodels

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.msartore.ares.R
import dev.msartore.ares.base.MviViewModel
import dev.msartore.ares.models.ConcurrentMutableList
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDownload
import dev.msartore.ares.models.FileType
import dev.msartore.ares.models.NetworkInfo
import dev.msartore.ares.models.Settings
import dev.msartore.ares.models.TransferFile
import dev.msartore.ares.models.TransferFileType
import dev.msartore.ares.ui.destinations.MainEvent
import dev.msartore.ares.ui.destinations.MainSideEffect
import dev.msartore.ares.ui.destinations.MainState
import dev.msartore.ares.utils.BackgroundPStatus
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val settings: Settings,
    val client: HttpClient,
    val downloadManager: DownloadManager,
    private val clipboard: ClipboardManager,
) : MviViewModel<MainState, MainEvent, MainSideEffect>(MainState()) {

    var transferredFiles: SnapshotStateList<TransferFile> = mutableStateListOf()
    val networkInfo = NetworkInfo()
    val qrCodeDialog = mutableStateOf(false)
    var listFileDownload = ConcurrentMutableList<FileDownload>()
    val isDarkTheme = MutableStateFlow(false)
    var backgroundPStatus: MutableState<BackgroundPStatus?> = mutableStateOf(null)

    override suspend fun reduce(event: MainEvent) {
        when (event) {
            is MainEvent.FileDownloadCompleted -> {
                event.fileData?.let {
                    transferredFiles.add(TransferFile(it, TransferFileType.DOWNLOAD))
                }
            }
            is MainEvent.FileUploadCompleted -> {
                event.fileData?.let {
                    transferredFiles.add(TransferFile(it, TransferFileType.UPLOAD))
                }
            }
            is MainEvent.OpenFileDownload -> {
                val fileDownload = event.fileDownload
                listFileDownload.removeIf { it == fileDownload }
                fileDownload.fileData
                    ?.let { emitSideEffect(MainSideEffect.OpenFile(it)) }
                    ?: emitSideEffect(MainSideEffect.ShowToast(R.string.no_app_can_perform))
            }
            is MainEvent.ShareFileDownload -> {
                val fileDownload = event.fileDownload
                listFileDownload.removeIf { it == fileDownload }
                fileDownload.fileData?.let { emitSideEffect(MainSideEffect.ShareFile(it)) }
            }
            is MainEvent.DismissFileDownload -> {
                listFileDownload.removeIf { it == event.fileDownload }
            }
            is MainEvent.CopyTextRequested -> {
                emitSideEffect(MainSideEffect.CopyToClipboard(event.label, event.text))
            }
            is MainEvent.ShareTextRequested -> {
                emitSideEffect(MainSideEffect.ShareText(event.text))
            }
            is MainEvent.OpenFileRequested -> {
                emitSideEffect(MainSideEffect.OpenFile(event.fileData))
            }
            is MainEvent.ShareFileRequested -> {
                emitSideEffect(MainSideEffect.ShareFile(event.fileData))
            }
            is MainEvent.OpenStreamingRequested -> {
                val mime = when (event.fileType) {
                    FileType.VIDEO -> "video/*"
                    FileType.IMAGE -> "image/*"
                    else -> "*/*"
                }
                emitSideEffect(MainSideEffect.LaunchStreamingIntent(event.url, mime))
            }
            is MainEvent.UrlOpened -> {
                emitSideEffect(MainSideEffect.OpenUrl(event.url))
            }
            MainEvent.BackgroundClick -> { /* handled by MainActivity via onBackgroundClick lambda */ }
            MainEvent.DismissQrCodeDialog -> {
                qrCodeDialog.value = false
            }
        }
    }

    fun copyText(label: String, string: String) {
        val clip = ClipData.newPlainText(label, string)
        clipboard.setPrimaryClip(clip)
    }

    fun openUrl(url: String) {
        viewModelScope.launch { onEvent(MainEvent.UrlOpened(url)) }
    }

    fun openStreaming(url: String, fileType: FileType?) {
        viewModelScope.launch { onEvent(MainEvent.OpenStreamingRequested(url, fileType)) }
    }

    fun hasCamera(packageManager: PackageManager): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}
