package dev.msartore.ares.ui.screens.serverFinder

import android.net.nsd.NsdServiceInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.msartore.ares.R
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.components.DialogContainer
import dev.msartore.ares.ui.components.TextAuto
import dev.msartore.ares.ui.components.views.CameraUI
import dev.msartore.ares.ui.navigation.MainRoutes
import dev.msartore.ares.utils.Permissions
import dev.msartore.ares.utils.getIpAndPort
import dev.msartore.ares.utils.pingServer
import dev.msartore.ares.utils.work
import dev.msartore.ares.ui.screens.main.MainViewModel
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPermissionsApi::class)
fun NavGraphBuilder.serverFinderDestination(
    mainViewModel: MainViewModel,
    nsdFlow: Flow<NsdServiceInfo?>,
    navigateToSettingsScreen: () -> Unit,
) {
    composable(MainRoutes.SERVER_FINDER.route) {
        val viewModel: ServerFinderViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            nsdFlow.collect { nsdInfo ->
                if (nsdInfo == null) {
                    viewModel.onEvent(ServerFinderEvent.AllServersLost)
                } else {
                    getIpAndPort(nsdInfo).let { pair ->
                        pair.first?.let { ip ->
                            viewModel.onEvent(
                                ServerFinderEvent.ServerDiscovered(
                                    ServerInfo(ip = ip, port = pair.second.toString())
                                )
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    ServerFinderSideEffect.LaunchQrScanner -> viewModel.scanQRCode()
                }
            }
        }

        ServerFinderUI(
            state = state,
            onEvent = viewModel::onEvent,
            mainViewModel = mainViewModel,
        )

        viewModel.qrReadingProcess.apply {
            val loadingStatusDialog = remember { mutableStateOf(false) }

            LaunchedEffect(isPingingServer.value) {
                loadingStatusDialog.value = isPingingServer.value
            }

            if (isReadingQR.value) {
                val permissionState = rememberMultiplePermissionsState(
                    permissions = listOf(android.Manifest.permission.CAMERA)
                )

                BackHandler(true) { isReadingQR.value = false }

                Permissions(
                    permissionState = permissionState,
                    requestStringId = R.string.camera_permission_request_text,
                    settingsStringId = R.string.camera_permission_rejected_text,
                    navigateToSettingsScreen = navigateToSettingsScreen,
                    onPermissionDenied = { isReadingQR.value = false },
                    onPermissionGranted = {
                        CameraUI(visibility = isReadingQR) { ip, port ->
                            isReadingQR.value = false
                            isPingingServer.value = true
                            work {
                                runCatching {
                                    mainViewModel.settings.pingServer(ip, 2000)
                                    viewModel.onEvent(ServerFinderEvent.ServerDiscovered(ServerInfo(ip = ip, port = port)))
                                }.onFailure {
                                    errorStatusDialog.value = true
                                }
                                isPingingServer.value = false
                            }
                        }
                    }
                )
            }

            DialogContainer(
                status = errorStatusDialog,
                dialogProperties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextAuto(id = R.string.server_not_found, maxLines = Int.MAX_VALUE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { errorStatusDialog.value = false }) {
                            TextAuto(id = R.string.close)
                        }
                    }
                }
            }

            DialogContainer(status = loadingStatusDialog) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(35.dp))
                }
            }
        }
    }
}