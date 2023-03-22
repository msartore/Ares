package dev.msartore.ares

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileType
import dev.msartore.ares.models.NetworkCallback
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.ui.theme.AresTheme
import dev.msartore.ares.ui.views.MainUI
import dev.msartore.ares.utils.Permissions
import dev.msartore.ares.utils.extractFileInformation
import dev.msartore.ares.utils.filesDataHandler
import dev.msartore.ares.utils.findServers
import dev.msartore.ares.utils.isWideView
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.HomeViewModel
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.ServerFinderViewModel
import dev.msartore.ares.viewmodels.SettingsViewModel
import kotlinx.coroutines.runBlocking


@ExperimentalGetImage
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val serverFinderViewModel: ServerFinderViewModel by viewModels()
    private var service: Intent? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: NetworkCallback? = null
    private var receiver: BroadcastReceiver? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                mainViewModel.fileDownload.run {
                    fileData =
                        mainViewModel.downloadManager?.getUriForDownloadedFile(reference)
                            ?.let { context?.contentResolver?.extractFileInformation(it) }
                    state.value = true
                    mainViewModel.fileDownload.timerScheduler?.start()
                }
            }
        }
        registerReceiver(receiver, filter)

        val getContent =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                work {
                    filesDataHandler(homeViewModel.isLoading, uris)
                }
            }
        val getDownload =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                mainViewModel.fileDownload.state.value = false
            }
        var permissionState: MultiplePermissionsState? = null
        val getContentPermission =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (permissionState?.allPermissionsGranted == false) finishAffinity()
            }
        val intentSettings = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        val navigateToSettingsScreen = {
            getContentPermission.launch(intentSettings)
        }

        service = Intent(this, KtorService::class.java)

        runBlocking {
            connectivityManager = getSystemService(ConnectivityManager::class.java)
            networkCallback =
                NetworkCallback(networkInfo = mainViewModel.networkInfo, onNetworkLost = {
                    stopService(service)
                })

            networkCallback?.let {
                connectivityManager?.registerDefaultNetworkCallback(it)
            }

            mainViewModel.run {
                clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                pm = packageManager
                onFindServers = { _settings, _networkInfo ->
                    findServers(
                        _networkInfo, _settings, serverFinderViewModel.ipSearchData
                    )
                }
                downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
                onOpenUrl = { url ->
                    runCatching {
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        })
                    }.getOrElse {
                        it.printStackTrace()
                    }
                }
                openFile = {
                    mainViewModel.fileDownload.timerScheduler?.cancel()
                    getDownload.launch(
                        Intent(Intent.ACTION_VIEW).apply {
                            fileDownload.fileData?.run {
                                setDataAndType(
                                    uri,
                                    mimeType
                                )
                            }
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    )
                }
                startSettings()
            }

            settingsViewModel.onOpenThirdLicenses = {
                startActivity(Intent(applicationContext, OssLicensesMenuActivity::class.java))
            }

            homeViewModel.run {
                onImportFiles = {
                    getContent.launch(arrayOf("*/*"))
                }
                onStopServer = {
                    stopService(service)
                }
                onStartServer = {
                    startForegroundService(service)
                }
            }

            if ("text/plain" == intent.type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    concurrentMutableList.add(
                        FileData(
                            fileType = FileType.TEXT, text = it
                        )
                    )
                }

                if (!KtorService.KtorServer.isServerOn.value && mainViewModel.settings?.serverAutoStartup?.value == true) homeViewModel.onStartServerClick()
            } else if (intent.clipData != null) {
                runBlocking {
                    val listUri = mutableListOf<Uri>()

                    for (i in 0 until (intent.clipData?.itemCount ?: 0)) {
                        intent.clipData?.getItemAt(i)?.uri?.let {
                            listUri.add(it)
                        }
                    }

                    if (listUri.isNotEmpty()) {
                        filesDataHandler(homeViewModel.isLoading, listUri)

                        if (!KtorService.KtorServer.isServerOn.value && mainViewModel.settings?.serverAutoStartup?.value == true) homeViewModel.onStartServerClick()
                    }
                }
            }
        }

        setContent {
            val isNavBarColorSet = remember { mutableStateOf(false) }
            val resetStatusBarColor = remember { mutableStateOf({}) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionState =
                rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.POST_NOTIFICATIONS))

            AresTheme(
                changeStatusBarColor = resetStatusBarColor,
                isNavBarColorSet = isNavBarColorSet,
                mainViewModel = mainViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Permissions(permissionState = permissionState,
                        settingsStringId = R.string.notification_permission_rejected_text,
                        requestStringId = R.string.notification_permission_request_text,
                        navigateToSettingsScreen = navigateToSettingsScreen,
                        onPermissionDenied = {
                            finishAffinity()
                        },
                        onPermissionGranted = {
                            BoxWithConstraints {
                                val maxWidth = this.maxWidth

                                isNavBarColorSet.value = !maxWidth.isWideView()

                                MainUI(
                                    navigateToSettingsScreen = navigateToSettingsScreen,
                                    mainViewModel = mainViewModel,
                                    maxWidth = maxWidth
                                )
                            }
                        })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.client.close()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        unregisterReceiver(receiver)
    }
}