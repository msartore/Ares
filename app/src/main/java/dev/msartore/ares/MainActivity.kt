package dev.msartore.ares

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.msartore.ares.MainActivity.MActivity.client
import dev.msartore.ares.MainActivity.MActivity.dataStore
import dev.msartore.ares.MainActivity.MActivity.downloadManager
import dev.msartore.ares.MainActivity.MActivity.isDarkTheme
import dev.msartore.ares.models.IPSearchData
import dev.msartore.ares.models.NetworkCallback
import dev.msartore.ares.models.NetworkInfo
import dev.msartore.ares.models.Settings
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.ui.theme.AresTheme
import dev.msartore.ares.ui.views.MainUI
import dev.msartore.ares.utils.Permissions
import dev.msartore.ares.utils.cor
import dev.msartore.ares.utils.extractFileInformation
import dev.msartore.ares.utils.findServers
import dev.msartore.ares.utils.getRightPermissions
import dev.msartore.ares.utils.work
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    object MActivity {
        val Context.dataStore by preferencesDataStore(
            name = "user_preferences_settings"
        )
        val isDarkTheme = mutableStateOf(false)
        val networkInfo = NetworkInfo()
        val ipSearchData = IPSearchData()
        val client: HttpClient = HttpClient(CIO) {
            install(HttpTimeout)
        }
        var downloadManager: DownloadManager? = null
    }

    private var service: Intent? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: NetworkCallback? = null
    private var settings: Settings? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        service = Intent(this, KtorService::class.java)
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        networkCallback = NetworkCallback(
            onNetworkLost = {
                stopService(service)
            }
        )

        val isLoading = mutableStateOf(false)
        var fileAndMediaPermissionState: MultiplePermissionsState? = null
        val getContentPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (fileAndMediaPermissionState?.allPermissionsGranted == false) finishAffinity()
        }
        val getContent = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->

            val listFileSizeB = concurrentMutableList.size.value

            work {
                if (!uris.isNullOrEmpty()) {
                    isLoading.value = true

                    concurrentMutableList.apply {
                        addAll(
                            uris.filter { uri ->
                                this.list.none { it.uri == uri }
                            }.mapNotNull {
                                contentResolver.extractFileInformation(it)
                            }
                        )
                    }

                    if (listFileSizeB == concurrentMutableList.size.value)
                        cor {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.removed_duplicates),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    isLoading.value = false
                }
            }
        }
        val openUrl: (String) -> Unit = {
            runCatching {
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(it)
                    }
                )
            }.getOrElse {
                it.printStackTrace()
            }
        }
        val intentSettings = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )

        settings = Settings(dataStore)

        cor {
            networkCallback?.let {
                connectivityManager?.registerDefaultNetworkCallback(it)
            }

            settings?.update()

            if (settings?.findServersAtStart?.value == true)
                findServers(settings)
        }

        setContent {

            val resetStatusBarColor = remember { mutableStateOf({}) }
            fileAndMediaPermissionState = rememberMultiplePermissionsState(permissions = getRightPermissions())

            AresTheme(
                changeStatusBarColor = resetStatusBarColor,
                isDarkTheme = isDarkTheme,
                settings = settings
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Permissions(
                        fileAndMediaPermissionState = fileAndMediaPermissionState,
                        navigateToSettingsScreen = {
                            getContentPermission.launch(intentSettings)
                        },
                        onPermissionDenied = {
                            finishAffinity()
                        },
                        onPermissionGranted = {
                            MainUI(
                                settings = settings!!,
                                isLoading = isLoading,
                                openUrl = openUrl,
                                onImportFilesClick = {
                                    getContent.launch(arrayOf("*/*"))
                                },
                                onStartServerClick = {
                                    startForegroundService(service)
                                },
                                onStopServerClick = {
                                    stopService(service)
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
    }
}