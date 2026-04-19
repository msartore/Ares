package dev.msartore.ares.ui.serverFinder

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.ui.compose.CardIcon
import dev.msartore.ares.ui.compose.ServerItem
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.ui.home.MainEvent
import dev.msartore.ares.viewmodels.MainViewModel

@Composable
fun ServerFinderUI(
    mainViewModel: MainViewModel, serverFinderViewModel: ServerFinderViewModel
) {
    val context = LocalContext.current
    val state = rememberLazyGridState()
    
    var selectedPage by mutableStateOf(ServerFinderPages.SERVER_LIST)

    val pageTransition = updateTransition(
        targetState = selectedPage,
        label = selectedPage.name
    )

    val mainContent: @Composable (Modifier) -> Unit = { modifier ->
        mainViewModel.networkInfo.run {
            if (isNetworkAvailable.value && (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true)) {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    when {
                        serverFinderViewModel.state.value.servers.isNotEmpty() -> {
                            TextAuto(id = R.string.servers)

                            LazyVerticalGrid(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                columns = GridCells.Adaptive(250.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                state = state
                            ) {
                                items(
                                    items = serverFinderViewModel.state.value.servers
                                ) { server ->
                                    ServerItem(
                                        ip = server.ip,
                                        url = "http://${server.ip}:${port}",
                                        openUrl = { url ->
                                            mainViewModel.onEvent(MainEvent.UrlOpened(url))
                                        }) {
                                            serverFinderViewModel.onEvent(ServerFinderEvent.ServerSelected(server))
                                    }
                                }
                            }
                        }

                        else -> {
                            TextAuto(
                                id = R.string.no_server_found, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.weight(9f, false),
                        painter = painterResource(id = R.drawable.no_connection_rafiki),
                        contentDescription = stringResource(id = R.string.wrong_network)
                    )

                    TextAuto(
                        modifier = Modifier.weight(1f, false), id = R.string.wrong_network
                    )
                }
            }
        }
    }

    BackHandler(selectedPage == ServerFinderPages.SERVER) {
        serverFinderViewModel.onEvent(ServerFinderEvent.BackToServerList)
    }

    pageTransition.AnimatedContent {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                ServerFinderPages.SERVER_LIST -> {
                    mainContent(Modifier.weight(8f))
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) CardIcon(
                            iconId = R.drawable.qr_code_scanner_24px,
                            textId = R.string.scan_qrcode,
                            contentDescription = stringResource(id = R.string.scan_qrcode),
                        ) {
                            serverFinderViewModel.onEvent(ServerFinderEvent.OpenQrScanner)
                        }
                    }
                }

                ServerFinderPages.SERVER -> {
                    serverFinderViewModel.state.value.serverSelected?.let { serverInfo ->
                        dev.msartore.ares.ui.views.ServerUI(
                            serverInfo = serverInfo,
                            mainViewModel = mainViewModel,
                            serverFinderViewModel = serverFinderViewModel
                        )
                    }
                }
            }
        }
    }
}