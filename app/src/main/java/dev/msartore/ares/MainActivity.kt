package dev.msartore.ares

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipData
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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileType
import dev.msartore.ares.models.NetworkCallback
import dev.msartore.ares.models.NetworkDiscoveryService
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.ui.screens.main.MainEvent
import dev.msartore.ares.ui.screens.main.MainSideEffect
import dev.msartore.ares.ui.navigation.AresNavHost
import dev.msartore.ares.ui.theme.AresTheme
import dev.msartore.ares.utils.BackgroundPStatus
import dev.msartore.ares.utils.Permissions
import dev.msartore.ares.utils.checkForBackgroundPermission
import dev.msartore.ares.utils.cleanCache
import dev.msartore.ares.utils.extractFileInformation
import dev.msartore.ares.utils.filesDataHandler
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.MainViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import android.provider.Settings as AndroidSettings


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var nsdFlow: MutableSharedFlow<NsdServiceInfo?>
    @Inject lateinit var networkDiscoveryService: NetworkDiscoveryService
    @Inject lateinit var connectivityManager: ConnectivityManager

    private var service: Intent? = null
    private var networkCallback: NetworkCallback? = null
    private var receiver: BroadcastReceiver? = null
    private var activityManager: ActivityManager? = null
    private var powerManager: PowerManager? = null

    @SuppressLint("BatteryLife")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionStateList = mutableListOf(Manifest.permission.WAKE_LOCK)

        activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        powerManager = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        service = Intent(this, KtorService::class.java)
        applicationContext.cleanCache()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                mainViewModel.downloadManager.getUriForDownloadedFile(reference)
                    ?.let { context?.contentResolver?.extractFileInformation(it) }
                    ?.let { mainViewModel.onEvent(MainEvent.FileDownloadCompleted(it)) }
            }
        }

        val intentBatteryOptimization = Intent(AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        var permissionState: MultiplePermissionsState? = null
        val getContentPermission =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (permissionState?.allPermissionsGranted == false) finishAffinity()
            }
        val intentSettings = Intent(
            AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        val navigateToSettingsScreen = { getContentPermission.launch(intentSettings) }

        val isBackgroundDialogVisible = mutableStateOf(false)
        val getContentBackgroundPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            work {
                kotlinx.coroutines.delay(500)
                mainViewModel.backgroundPStatus.value = checkForBackgroundPermission(powerManager, activityManager, packageName)
            }
        }
        val onBackgroundClick = { getContentBackgroundPermission.launch(intentBatteryOptimization) }

        val getContent =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                work {
                    filesDataHandler({}, uris)
                }
            }

        networkCallback = NetworkCallback(networkInfo = mainViewModel.networkInfo, onNetworkLost = {
            stopService(service)
        })
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)

        KtorService.KtorServer.fileTransfer.onFileTransferred = { file ->
            FileProvider.getUriForFile(
                applicationContext, applicationContext.packageName + ".provider", file
            ).run {
                contentResolver.extractFileInformation(this)
                    ?.let { mainViewModel.onEvent(MainEvent.FileUploadCompleted(it)) }
            }
        }

        mainViewModel.backgroundPStatus.value = checkForBackgroundPermission(powerManager, activityManager, packageName)
        mainViewModel.settings.apply {
            isBackgroundDialogVisible.value = mainViewModel.backgroundPStatus.value == BackgroundPStatus.NOT_OPTIMIZED &&
                    (requestBackgroundActivity.value < 2)
        }

        work {
            mainViewModel.settings.update()
        }

        handleIncomingShareIntent()

        networkDiscoveryService.createServices(applicationContext)

        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                permissionStateList.add(Manifest.permission.POST_NOTIFICATIONS)

            permissionState = rememberMultiplePermissionsState(permissions = permissionStateList)

            AresTheme(mainViewModel = mainViewModel) {
                // Collect MainViewModel SideEffects at root level
                LaunchedEffect(Unit) {
                    mainViewModel.sideEffects.collect { effect ->
                        when (effect) {
                            is MainSideEffect.CopyToClipboard -> {
                                val mgr = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                mgr.setPrimaryClip(ClipData.newPlainText(effect.label, effect.text))
                            }
                            is MainSideEffect.ShareText -> {
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, effect.text)
                                        }, getString(R.string.send_to)
                                    )
                                )
                            }
                            is MainSideEffect.OpenFile -> {
                                runCatching {
                                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(effect.fileData.uri, effect.fileData.mimeType)
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    })
                                }.onFailure {
                                    Toast.makeText(applicationContext, getString(R.string.no_app_can_perform), Toast.LENGTH_LONG).show()
                                }
                            }
                            is MainSideEffect.ShareFile -> {
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            putExtra(Intent.EXTRA_STREAM, effect.fileData.uri)
                                            type = effect.fileData.mimeType
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }, getString(R.string.send_to)
                                    )
                                )
                            }
                            is MainSideEffect.OpenUrl -> {
                                runCatching {
                                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                                        data = effect.url.toUri()
                                    })
                                }.onFailure { it.printStackTrace() }
                            }
                            is MainSideEffect.LaunchStreamingIntent -> {
                                startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(effect.url.toUri(), effect.mimeType)
                                })
                            }
                            is MainSideEffect.ShowToast -> {
                                Toast.makeText(applicationContext, effect.messageRes, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                Permissions(
                    permissionState = permissionState,
                    settingsStringId = R.string.notification_permission_rejected_text,
                    requestStringId = R.string.notification_permission_request_text,
                    navigateToSettingsScreen = navigateToSettingsScreen,
                    onPermissionDenied = { finishAffinity() },
                    onPermissionGranted = {
                        AresNavHost(
                            mainViewModel = mainViewModel,
                            nsdFlow = nsdFlow,
                            httpClient = mainViewModel.client,
                            navigateToSettingsScreen = navigateToSettingsScreen,
                            onLaunchFilePicker = { getContent.launch(arrayOf("*/*")) },
                            onStartServer = { startForegroundService(service) },
                            onStopServer = { stopService(service) },
                            onBackgroundClick = onBackgroundClick,
                            onLaunchOssLicenses = {
                                startActivity(Intent(applicationContext, OssLicensesMenuActivity::class.java))
                            },
                        )
                    }
                )

                dev.msartore.ares.ui.components.Dialog(
                    status = isBackgroundDialogVisible,
                    title = getString(R.string.permission_request),
                    text = getString(R.string.background_permission_restriction_description),
                    onConfirm = {
                        isBackgroundDialogVisible.value = false
                        mainViewModel.settings.apply {
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
                            mainViewModel.settings.apply {
                                requestBackgroundActivity.value++
                                save(dev.msartore.ares.models.Settings.Keys.RequestBackgroundActivity, requestBackgroundActivity)
                            }
                        }
                    }
                )
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
        } else {
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
        stopService(service)
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        unregisterReceiver(receiver)
        applicationContext.cleanCache()
        networkDiscoveryService.tearDown()
        super.onDestroy()
    }

    private fun handleIncomingShareIntent() {
        if ("text/plain" == intent.type) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                concurrentMutableList.add(FileData(fileType = FileType.TEXT, text = it))
            }
        } else if (intent.clipData != null) {
            val listUri = mutableListOf<Uri>()
            for (i in 0 until (intent.clipData?.itemCount ?: 0)) {
                intent.clipData?.getItemAt(i)?.uri?.let { listUri.add(it) }
            }
            if (listUri.isNotEmpty()) {
                work { filesDataHandler({}, listUri) }
            }
        }

        if (!KtorService.KtorServer.isServerOn.value &&
            mainViewModel.settings.serverAutoStartup.value &&
            intent.type?.isNotEmpty() == true
        ) {
            startForegroundService(service)
        }
    }
}
