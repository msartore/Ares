package dev.msartore.ares

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.window.core.layout.WindowWidthSizeClass
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDownload
import dev.msartore.ares.models.FileType
import dev.msartore.ares.models.NetworkCallback
import dev.msartore.ares.models.NetworkDiscoveryService
import dev.msartore.ares.models.TransferFile
import dev.msartore.ares.models.TransferFileType
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.Dialog
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.ui.theme.AresTheme
import dev.msartore.ares.ui.views.MainPages
import dev.msartore.ares.ui.views.MainUI
import dev.msartore.ares.utils.BackgroundPStatus
import dev.msartore.ares.utils.Permissions
import dev.msartore.ares.utils.checkForBackgroundPermission
import dev.msartore.ares.utils.cleanCache
import dev.msartore.ares.utils.cor
import dev.msartore.ares.utils.extractFileInformation
import dev.msartore.ares.utils.filesDataHandler
import dev.msartore.ares.utils.getIpAndPort
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.HomeViewModel
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.ServerFinderViewModel
import dev.msartore.ares.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private var activityManager: ActivityManager? = null
    private var powerManager: PowerManager? = null
    private var networkDiscoveryService: NetworkDiscoveryService? = null
    private val servers: MutableSharedFlow<NsdServiceInfo?> = MutableStateFlow(null)


    @SuppressLint("BatteryLife")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionStateList = mutableListOf(Manifest.permission.WAKE_LOCK)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val fileData = mainViewModel.downloadManager?.getUriForDownloadedFile(reference)
                    ?.let { context?.contentResolver?.extractFileInformation(it) }
                if (!fileData?.name.isNullOrEmpty()) {
                    fileData?.let {
                        mainViewModel.transferredFiles.add(
                            TransferFile(it, TransferFileType.DOWNLOAD)
                        )
                    }
                    mainViewModel.listFileDownload.add(FileDownload(
                        fileData = fileData,
                        onFinish = {
                            mainViewModel.listFileDownload.removeIf { it.fileData?.uri == fileData?.uri }
                        }).also {
                        it.timerScheduler?.start()
                    })
                }
            }
        }

        val getContent =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                work {
                    filesDataHandler(homeViewModel.isLoading, uris)
                }
            }
        val intentBatteryOptimization = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }

        var permissionState: MultiplePermissionsState? = null
        val getContentPermission =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (permissionState?.allPermissionsGranted == false) finishAffinity()
            }
        val intentSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        val navigateToSettingsScreen = {
            getContentPermission.launch(intentSettings)
        }
        val isBackgroundDialogVisible = mutableStateOf(false)
        val getContentBackgroundPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            work {
                delay(500)
                mainViewModel.backgroundPStatus.value = checkForBackgroundPermission(powerManager, activityManager, packageName)
            }
        }

        cor {
            servers.collectLatest {
                if (it == null) serverFinderViewModel.clearServers()
                else {
                    getIpAndPort(it).let { pair ->
                        pair.first?.let { ip ->
                            serverFinderViewModel.addServer(ServerInfo(ip = ip, port = pair.second.toString()))
                        }
                    }
                }
            }
        }

        activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        powerManager = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        service = Intent(this, KtorService::class.java)
        applicationContext.cleanCache()

        runBlocking {
            connectivityManager = getSystemService(ConnectivityManager::class.java)
            networkCallback =
                NetworkCallback(networkInfo = mainViewModel.networkInfo, onNetworkLost = {
                    stopService(service)
                })

            val builder = NetworkRequest.Builder()
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)

            val networkRequest = builder.build()

            networkCallback?.let {
                connectivityManager?.registerNetworkCallback(networkRequest, it)
            }

            KtorService.KtorServer.fileTransfer.onFileTransferred = { file ->
                FileProvider.getUriForFile(
                    applicationContext, applicationContext.packageName + ".provider", file
                ).run {
                    contentResolver.extractFileInformation(this).run {
                        this?.let { mainViewModel.transferredFiles.add(
                            TransferFile(it, TransferFileType.UPLOAD)
                        )}
                        mainViewModel.listFileDownload.add(FileDownload(fileData = this,
                            onFinish = {
                                mainViewModel.listFileDownload.removeIf { it.fileData?.uri == this?.uri }
                            }).also {
                            it.timerScheduler?.start()
                        })
                    }
                }
            }

            mainViewModel.run {
                startSettings()

                work {
                    clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    pm = packageManager
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

                    onOpenFile = {
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            it.run {
                                setDataAndType(
                                    uri, mimeType
                                )
                            }
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        })
                    }
                    onShareFile = {
                        it.run {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = mimeType
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            startActivity(Intent.createChooser(intent, getString(R.string.send_to)))
                        }
                    }
                    onOpenFileDownload = { fileDownload ->
                        fileDownload.timerScheduler?.cancel()
                        runCatching {
                            fileDownload.fileData?.let {
                                onOpenFile?.invoke(it)
                            }
                        }.onFailure {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.no_app_can_perform),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        listFileDownload.removeIf { it == fileDownload }
                    }
                    onShareFileDownload = { fileDownload ->
                        fileDownload.fileData?.let {
                            onShareFile?.invoke(it)
                        }
                        listFileDownload.removeIf { it == fileDownload }
                    }
                    onDismiss = { fileDownload ->
                        listFileDownload.removeIf { it == fileDownload }
                    }
                    onBackgroundClick = {
                        getContentBackgroundPermission.launch(intentBatteryOptimization)
                    }

                    settings?.apply {
                        backgroundPStatus.value = checkForBackgroundPermission(powerManager, activityManager, packageName)
                        isBackgroundDialogVisible.value = backgroundPStatus.value == BackgroundPStatus.NOT_OPTIMIZED &&
                                (settings?.requestBackgroundActivity?.value?: 2) < 2
                    }
                }
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
                    }
                }
            }

            if (!KtorService.KtorServer.isServerOn.value && mainViewModel.settings?.serverAutoStartup?.value == true && intent.type?.isNotEmpty() == true)
                homeViewModel.onStartServerClick()
        }

        networkDiscoveryService = NetworkDiscoveryService(servers)
        networkDiscoveryService?.createServices(applicationContext)

        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                permissionStateList.add(Manifest.permission.POST_NOTIFICATIONS)

            permissionState = rememberMultiplePermissionsState(permissions = permissionStateList)

            AresTheme(
                mainViewModel = mainViewModel
            ) {
                val items = remember { listOf(MainPages.HOME, MainPages.SERVER_FINDER, MainPages.TRANSFERS, MainPages.SETTINGS) }
                val selectedItem = remember { mutableStateOf(MainPages.HOME) }
                val icon: @Composable (MainPages) -> Unit = { page ->

                    if (page == MainPages.TRANSFERS) {
                        BadgedBox(
                            badge = {
                                val count = mainViewModel.transferredFiles.count { !it.viewed.value }

                                if (count > 0)
                                    Badge {
                                        TextAuto(text = count.toString())
                                    }
                            }
                        ) {
                            Icon(
                                id = R.drawable.download_24px
                            )
                        }
                    }
                    else
                        Icon(
                            id = when (page) {
                                MainPages.HOME -> {
                                    if (selectedItem.value == page) R.drawable.home_filled_24px
                                    else R.drawable.home_24px
                                }

                                MainPages.SERVER_FINDER -> {
                                    if (selectedItem.value == page) R.drawable.wifi_find_filled_24px
                                    else R.drawable.wifi_find_24px
                                }

                                else -> {
                                    if (selectedItem.value == page) R.drawable.settings_filled_24px
                                    else R.drawable.settings_24px
                                }
                            }, contentDescription = stringResource(id = page.stringId)
                        )
                }
                val onClick: (MainPages) -> Unit = { page ->
                    if (selectedItem.value != page) selectedItem.value = page
                }
                var maxWidth = remember { mutableStateOf(0.dp) }
                val adaptiveInfo = currentWindowAdaptiveInfo()

                NavigationSuiteScaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(if (adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) 16.dp else 0.dp),
                    navigationSuiteColors = NavigationSuiteDefaults.colors(
                        navigationBarContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        navigationRailContentColor = MaterialTheme.colorScheme.secondaryContainer,
                        navigationDrawerContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    navigationSuiteItems = {
                        items.forEach { item ->
                            item(
                                icon = {
                                    icon(item)
                                },
                                label = { Text(stringResource(item.stringId)) },
                                selected = selectedItem.value == item,
                                onClick = { onClick(item) }
                            )
                        }
                    },
                )  {
                    Permissions(permissionState = permissionState,
                        settingsStringId = R.string.notification_permission_rejected_text,
                        requestStringId = R.string.notification_permission_request_text,
                        navigateToSettingsScreen = navigateToSettingsScreen,
                        onPermissionDenied = {
                            finishAffinity()
                        },
                        onPermissionGranted = {
                            BoxWithConstraints {
                                maxWidth.value = this.maxWidth

                                MainUI(
                                    navigateToSettingsScreen = navigateToSettingsScreen,
                                    mainViewModel = mainViewModel,
                                    maxWidth = maxWidth.value,
                                    selectedItem = selectedItem
                                )
                            }
                        })

                    Dialog(
                        status = isBackgroundDialogVisible,
                        title = stringResource(id = R.string.permission_request),
                        text = stringResource(id = R.string.background_permission_restriction_description),
                        onConfirm = {
                            isBackgroundDialogVisible.value = false
                            mainViewModel.settings?.apply {
                                work {
                                    requestBackgroundActivity.value++
                                    save(dev.msartore.ares.models.Settings.Keys.RequestBackgroundActivity, requestBackgroundActivity)
                                    getContentBackgroundPermission.launch(intentBatteryOptimization)
                                }
                            }
                        },
                        onCancel = {
                            isBackgroundDialogVisible.value = false
                            work {
                                mainViewModel.settings?.apply {
                                    requestBackgroundActivity.value++
                                    save(
                                        dev.msartore.ares.models.Settings.Keys.RequestBackgroundActivity,
                                        requestBackgroundActivity
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        work {
            mainViewModel.backgroundPStatus.value = checkForBackgroundPermission(powerManager, activityManager, packageName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        }
        else {
            registerReceiver(receiver, filter)
        }
    }

    override fun onDestroy() {
        KtorService.KtorServer.run {
            fileTransfer.run {
                if (file?.exists() == true) file?.delete()
            }
        }
        mainViewModel.client.close()
        homeViewModel.onStopServerClick()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        unregisterReceiver(receiver)
        applicationContext.cleanCache()
        networkDiscoveryService?.tearDown()
        super.onDestroy()
    }
}