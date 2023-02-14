package dev.msartore.ares.ui.views

import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.ui.compose.CardIcon
import dev.msartore.ares.ui.compose.ServerItem
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.findServers
import dev.msartore.ares.utils.isWideView
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.ServerFinderViewModel

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalGetImage
@Composable
fun ServerFinderUI(
    maxWidth: Dp,
    mainViewModel: MainViewModel,
    serverFinderViewModel: ServerFinderViewModel
) {

    val state = rememberLazyGridState()
    val context = LocalContext.current
    val transition = updateTransition(serverFinderViewModel.selectedItem.value, label = serverFinderViewModel.selectedItem.value.name)
    val mainContent: @Composable (Modifier) -> Unit = { modifier ->

        mainViewModel.networkInfo.run {
            if (isNetworkAvailable.value && (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true)) {

                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                    if (serverFinderViewModel.ipSearchData.ipList.size.value == 0)
                        Arrangement.Center
                    else
                        Arrangement.Top
                ) {

                    when {
                        serverFinderViewModel.ipSearchData.ipList.size.value > 0 -> {
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
                                serverFinderViewModel.apply {
                                    items(
                                        count = ipSearchData.ipList.size.value,
                                        key = { ipSearchData.ipList.list.elementAt(it).hashCode() }
                                    ) {
                                        ServerItem(
                                            IP = ipSearchData.ipList.list.elementAt(it).ip,
                                            url = "http://${ipSearchData.ipList.list.elementAt(it).ip}:$port",
                                            openUrl = { url ->
                                                mainViewModel.openUrl(url)
                                            }
                                        ) {
                                            setServer(ipSearchData.ipList.list.elementAt(it))
                                        }
                                    }
                                }
                            }
                        }
                        serverFinderViewModel.ipSearchData.isSearching.value == 1 -> {
                            CircularProgressIndicator(modifier = Modifier.size(50.dp))
                        }
                        else -> {
                            TextAuto(
                                id = R.string.no_server_found,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            else {

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
                        modifier = Modifier.weight(1f, false),
                        id = R.string.wrong_network
                    )
                }
            }
        }
    }

    BackHandler(serverFinderViewModel.selectedItem.value == ServerFinderPages.SERVER) {
        serverFinderViewModel.backToScanWifi()
    }

    transition.AnimatedContent {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when (it) {
                ServerFinderPages.SCAN_WIFI -> {

                    if(maxWidth.isWideView())
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mainContent(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(8f)
                            )

                            mainViewModel.networkInfo.run {
                                if (isNetworkAvailable.value && (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true))
                                    Column(
                                        modifier = Modifier
                                            .weight(2f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {

                                        if (serverFinderViewModel.ipSearchData.isSearching.value == 0) {
                                            CardIcon(
                                                iconId = R.drawable.wifi_find_24px,
                                                textId = R.string.scan_network_for_servers,
                                                contentDescription = stringResource(id = R.string.scan_network_for_servers),
                                            ) {
                                                context.findServers(
                                                    settings = mainViewModel.settings,
                                                    networkInfo = mainViewModel.networkInfo,
                                                    ipSearchData = serverFinderViewModel.ipSearchData
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                        }

                                        if (mainViewModel.hasCamera())
                                            CardIcon(
                                                iconId = R.drawable.qr_code_scanner_24px,
                                                textId = R.string.scan_qrcode,
                                                contentDescription = stringResource(id = R.string.scan_qrcode),
                                            ) {
                                                serverFinderViewModel.scanQRCode()
                                            }
                                    }
                            }
                        }
                    else
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val scrollState = rememberScrollState()

                            mainContent(Modifier.weight(8f))

                            mainViewModel.networkInfo.run {
                                if (
                                    serverFinderViewModel.ipSearchData.isSearching.value == 0 &&
                                    isNetworkAvailable.value &&
                                    (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true)
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .verticalScroll(scrollState)
                                            .weight(2f),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        CardIcon(
                                            iconId = R.drawable.wifi_find_24px,
                                            textId = R.string.scan_network_for_servers,
                                            contentDescription = stringResource(id = R.string.scan_network_for_servers),
                                        ) {
                                            context.findServers(
                                                settings = mainViewModel.settings,
                                                networkInfo = mainViewModel.networkInfo,
                                                ipSearchData = serverFinderViewModel.ipSearchData
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (mainViewModel.hasCamera())
                                            CardIcon(
                                                iconId = R.drawable.qr_code_scanner_24px,
                                                textId = R.string.scan_qrcode,
                                                contentDescription = stringResource(id = R.string.scan_qrcode),
                                            ) {
                                                serverFinderViewModel.scanQRCode()
                                            }
                                    }
                                }
                            }
                        }
                }
                ServerFinderPages.SERVER -> {

                    ServerUI(
                        serverInfo = serverFinderViewModel.serverSelected.value,
                        mainViewModel = mainViewModel,
                        serverFinderViewModel = serverFinderViewModel
                    )
                }
            }
        }
    }
}

enum class ServerFinderPages {
    SCAN_WIFI,
    SERVER
}