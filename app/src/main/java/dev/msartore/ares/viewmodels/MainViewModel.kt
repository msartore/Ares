package dev.msartore.ares.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import dev.msartore.ares.R
import dev.msartore.ares.models.NetworkInfo
import dev.msartore.ares.models.Settings
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.MainViewModel.MVM.dataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {

    object MVM {
        val Context.dataStore by preferencesDataStore(name = "user_preferences_settings")
    }

    val networkInfo = NetworkInfo()
    val qrCodeDialog = mutableStateOf(false)
    val isDarkTheme = MutableStateFlow(false)
    val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout)
    }

    var pm: PackageManager? = null
    var settings: Settings? = null
    var onOpenUrl: ((String) -> Unit)? = null
    var downloadManager: DownloadManager? = null
    var onFindServers: ((NetworkInfo, Settings?) -> Unit)? = null

    fun Context.startSettings() {

        if (settings == null) {
            settings = Settings(dataStore = dataStore)
        }

        work {
            settings?.update()

            if (settings?.findServersAtStart?.value == true)
                onFindServers?.invoke(networkInfo, settings)
        }
    }

    fun Context.shareText(string: String) {
        startActivity(
            Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, string)
                },
                getString(R.string.send_to)
            )
        )
    }

    fun openUrl(url: String) {
        onOpenUrl?.invoke(url)
    }

    fun hasCamera() =
        pm?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true
}