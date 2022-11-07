package dev.msartore.ares.ui.views

import android.Manifest
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.DialogContainer
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.SnackBar
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.ui.compose.TransferDialog
import dev.msartore.ares.utils.Permissions
import dev.msartore.ares.utils.isWideView
import dev.msartore.ares.utils.pingServer
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.ServerFinderViewModel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@ExperimentalGetImage
@Composable
fun MainUI(
    navigateToSettingsScreen: () -> Unit,
    mainViewModel: MainViewModel,
    serverFinderViewModel: ServerFinderViewModel = viewModel(),
    maxWidth: Dp
) {
    val selectedItem = remember { mutableStateOf(MainPages.HOME) }
    val items = remember { listOf(MainPages.HOME, MainPages.SERVER_FINDER, MainPages.SETTINGS) }
    val transition = updateTransition(selectedItem.value, label = selectedItem.value.name)

    val icon: @Composable (MainPages) -> Unit = {
        Icon(
            id = when(it) {
                MainPages.HOME -> {
                    if (selectedItem.value == it)
                        R.drawable.home_filled_24px
                    else
                        R.drawable.home_24px
                }
                MainPages.SERVER_FINDER -> {
                    if (selectedItem.value == it)
                        R.drawable.wifi_find_filled_24px
                    else
                        R.drawable.wifi_find_24px
                }
                else -> {
                    if (selectedItem.value == it)
                        R.drawable.settings_filled_24px
                    else
                        R.drawable.settings_24px
                }
            },
            contentDescription = stringResource(id = it.stringId)
        )
    }
    val label: @Composable (MainPages) -> Unit = {
        TextAuto(
            id = it.stringId,
            style = MaterialTheme.typography.labelLarge
        )
    }
    val onClick: (MainPages) -> Unit = { page ->

        if (selectedItem.value != page)
            selectedItem.value = page
    }
    val mainUI: @Composable (PaddingValues?) -> Unit = { paddingValues ->
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = paddingValues?.calculateBottomPadding() ?: 16.dp
                )
        ) {
            transition.AnimatedContent {
                Column {
                    when(it) {
                        MainPages.HOME -> {
                            HomeUI(
                                maxWidth = maxWidth,
                                mainViewModel = mainViewModel
                            )
                        }
                        MainPages.SERVER_FINDER -> {
                            ServerFinderUI(
                                maxWidth = maxWidth,
                                mainViewModel = mainViewModel,
                                serverFinderViewModel = serverFinderViewModel
                            )
                        }
                        MainPages.SETTINGS -> {
                            SettingsUI(
                                mainViewModel = mainViewModel,
                            )
                        }
                    }
                }
            }

            SnackBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                visible = serverFinderViewModel.ipSearchData.isSearching.value != 0
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        TextAuto(id = R.string.scanning_servers)

                        TextAuto(text = "${stringResource(id = R.string.ip_left_check)}: ${serverFinderViewModel.ipSearchData.ipLeft.value}")
                    }

                    Icon(
                        id = R.drawable.cancel_24px
                    ) {
                        scope.launch {
                            serverFinderViewModel.apply {
                                ipSearchData.job.cancelAndJoin()
                                ipSearchData.isSearching.value = 0
                            }
                        }
                    }
                }
            }

            TransferDialog(
                status = KtorService.KtorServer.fileTransfer.isActive,
                progress = KtorService.KtorServer.fileTransfer.sizeTransferred
            )

            DialogContainer(
                status = mainViewModel.qrCodeDialog,
                dialogProperties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (mainViewModel.networkInfo.bitmap.value != null)
                        Image(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.onBackground,
                                    RoundedCornerShape(16.dp)
                                ),
                            bitmap = mainViewModel.networkInfo.bitmap.value!!,
                            contentDescription = "ip"
                        )
                    else
                        CircularProgressIndicator(
                            modifier = Modifier.size(80.dp),
                        )

                    Row(
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { mainViewModel.qrCodeDialog.value = false }) {
                            TextAuto(id = R.string.close)
                        }
                    }
                }
            }
        }
    }

    if (maxWidth.isWideView())
        Row {
            NavigationRail(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentWidth()
            ) {
                items.forEach { item ->
                    NavigationRailItem(
                        icon = { icon(item) },
                        label = { label(item) },
                        onClick = { onClick(item) },
                        selected = selectedItem.value == item
                    )
                }
            }

            mainUI(null)
        }
    else
        Scaffold(
            modifier = Modifier.fillMaxHeight(),
            bottomBar = {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { icon(item) },
                            label = { label(item) },
                            onClick = { onClick(item) },
                            selected = selectedItem.value == item
                        )
                    }
                }
            }
        ) {
            mainUI(it)
        }

    serverFinderViewModel.qrReadingProcess.apply {

        if (isReadingQR.value) {
            val permissionState = rememberMultiplePermissionsState(permissions = listOf(Manifest.permission.CAMERA))

            Permissions(
                permissionState = permissionState,
                requestStringId = R.string.camera_permission_request_text,
                settingsStringId = R.string.camera_permission_rejected_text,
                navigateToSettingsScreen = navigateToSettingsScreen,
                onPermissionDenied = {
                    isReadingQR.value = false
                },
                onPermissionGranted = {
                    CameraUI(
                        visibility = isReadingQR
                    ) { ip ->
                        isReadingQR.value = false
                        isPingingServer.value = true
                        isQRDialog.value = true

                        work {
                            runCatching {
                                ip.pingServer(
                                    settings = mainViewModel.settings,
                                    2000
                                )

                                serverFinderViewModel.ipSearchData.ipList.apply {
                                    if(list.none { it.ip == ip })
                                        add(ServerInfo(ip = ip))
                                }
                            }.onSuccess {
                                isQRDialog.value = false
                            }.onFailure {
                                isPingingServer.value = false
                            }
                        }
                    }
                }
            )
        }

        DialogContainer(status = isQRDialog) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isPingingServer.value)
                    CircularProgressIndicator(Modifier.size(35.dp))
                else {
                    TextAuto(
                        id = R.string.server_not_found,
                        maxLines = Int.MAX_VALUE
                    )

                    TextButton(onClick = { isQRDialog.value = false }) {
                        TextAuto(id = R.string.close)
                    }
                }
            }
        }
    }
}

enum class MainPages(val stringId: Int) {
    HOME(R.string.home),
    SERVER_FINDER(R.string.server_finder),
    SETTINGS(R.string.settings)
}