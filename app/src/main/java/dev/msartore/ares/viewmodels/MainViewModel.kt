package dev.msartore.ares.viewmodels

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import dev.msartore.ares.R
import dev.msartore.ares.models.ConcurrentMutableList
import dev.msartore.ares.models.FileDownload
import dev.msartore.ares.models.FileType
import dev.msartore.ares.models.NetworkInfo
import dev.msartore.ares.models.Settings
import dev.msartore.ares.utils.BackgroundPStatus
import dev.msartore.ares.viewmodels.MainViewModel.MVM.dataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.flow.MutableStateFlow


class MainViewModel : ViewModel() {

    object MVM {
        val Context.dataStore by preferencesDataStore(name = "user_preferences_settings")
    }

    var clipboard: ClipboardManager? = null
    val networkInfo = NetworkInfo()
    val qrCodeDialog = mutableStateOf(false)
    val listFileDownload = ConcurrentMutableList<FileDownload>()
    val isDarkTheme = MutableStateFlow(false)
    val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout)
    }
    var openFile: ((FileDownload) -> Unit)? = null
    var onDismiss: ((FileDownload) -> Unit)? = null
    var shareFile: ((FileDownload) -> Unit)? = null
    var pm: PackageManager? = null
    var settings: Settings? = null
    var onOpenUrl: ((String) -> Unit)? = null
    var downloadManager: DownloadManager? = null
    var onFindServers: ((NetworkInfo, Settings?) -> Unit)? = null
    var backgroundPStatus: MutableState<BackgroundPStatus?> = mutableStateOf(null)
    var onBackgroundClick: (() -> Unit)? = null

    @androidx.camera.core.ExperimentalGetImage
    suspend fun Context.startSettings() {

        if (settings == null) {
            settings = Settings(dataStore = dataStore)
        }

        settings?.update()

        if (settings?.findServersAtStart?.value == true) onFindServers?.invoke(
            networkInfo,
            settings
        )
    }

    fun copyText(label: String, string: String) {
        val clip = ClipData.newPlainText(label, string)
        clipboard?.setPrimaryClip(clip)
    }

    fun Context.shareText(string: String) {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, string)
                }, getString(R.string.send_to)
            )
        )
    }

    fun openStreaming(context: Context, url: String, fileType: FileType?) {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                Uri.parse(url), when (fileType) {
                    FileType.VIDEO -> {
                        "video/*"
                    }

                    FileType.IMAGE -> {
                        "image/*"
                    }

                    else -> {
                        "*/*"
                    }
                }
            )
        })
    }

    fun openUrl(url: String) {
        onOpenUrl?.invoke(url)
    }

    fun hasCamera() = pm?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true
}