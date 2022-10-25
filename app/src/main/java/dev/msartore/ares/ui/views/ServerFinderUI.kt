package dev.msartore.ares.ui.views

import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.IPItem
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.findServers
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.ServerFinderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalGetImage
@Composable
fun ServerFinderUI(
    mainViewModel: MainViewModel,
    serverFinderViewModel: ServerFinderViewModel
) {

    val state = rememberLazyGridState()
    val context = LocalContext.current
    val selectedItem = remember { mutableStateOf(ServerFinderPages.SCAN_WIFI) }
    val transition = updateTransition(selectedItem.value, label = selectedItem.value.name)
    val serverSelected = remember { mutableStateOf<ServerInfo?>(null) }
    val scope = rememberCoroutineScope()
    val backAction = {
        scope.launch {
            selectedItem.value = ServerFinderPages.SCAN_WIFI
            delay(200)
            serverSelected.value = null
        }
    }

    BackHandler(selectedItem.value == ServerFinderPages.SERVER) {
        backAction()
    }

    transition.AnimatedContent {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                ServerFinderPages.SCAN_WIFI ->
                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mainViewModel.networkInfo.isNetworkAvailable.value && mainViewModel.networkInfo.isWifiNetwork.value) {
                            if (serverFinderViewModel.ipSearchData.ipList.size.value > 0) {

                                Column(
                                    modifier = Modifier.weight(8f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    TextAuto(id = R.string.servers)

                                    LazyVerticalGrid(
                                        modifier = Modifier
                                            .padding(top = 16.dp),
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
                                                IPItem(
                                                    IP = ipSearchData.ipList.list.elementAt(it).ip,
                                                    url = "http://${ipSearchData.ipList.list.elementAt(it).ip}:$PORT",
                                                    openUrl = { url ->
                                                        mainViewModel.openUrl(url)
                                                    }
                                                ) {
                                                    selectedItem.value = ServerFinderPages.SERVER
                                                    serverSelected.value = ipSearchData.ipList.list.elementAt(it)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Column(
                                    modifier = Modifier.weight(9f, false),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        modifier = Modifier.weight(9f, false),
                                        painter = painterResource(id = R.drawable.all_the_data_rafiki),
                                        contentDescription = stringResource(id = R.string.no_server_found)
                                    )

                                    TextAuto(
                                        modifier = Modifier.weight(1f, false),
                                        id = R.string.no_server_found
                                    )
                                }
                            }

                            if (serverFinderViewModel.ipSearchData.isSearching.value == 0)
                                Column(
                                    modifier = Modifier
                                        .weight(2f, false)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            context.findServers(
                                                settings = mainViewModel.settings,
                                                networkInfo = mainViewModel.networkInfo,
                                                ipSearchData = serverFinderViewModel.ipSearchData
                                            )
                                        }
                                    ) {
                                        TextAuto(
                                            id = R.string.scan_network_for_servers,
                                        )
                                    }
                                    if (mainViewModel.hasCamera())
                                        Button(
                                            onClick = { serverFinderViewModel.scanQRCode() }
                                        ) {
                                            TextAuto(
                                                id = R.string.scan_qrcode,
                                            )
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
                ServerFinderPages.SERVER -> {
                    ServerUI(
                        serverInfo = serverSelected.value,
                        mainViewModel = mainViewModel,
                        backAction = backAction
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