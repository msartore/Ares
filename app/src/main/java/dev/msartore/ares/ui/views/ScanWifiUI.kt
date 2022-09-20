package dev.msartore.ares.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.msartore.ares.MainActivity.MActivity.ipSearchData
import dev.msartore.ares.MainActivity.MActivity.networkInfo
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.models.Settings
import dev.msartore.ares.ui.compose.IPItem
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.utils.findServers
import dev.msartore.ares.ui.compose.TextAuto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScanWifiUI(
    settings: Settings,
    openUrl: (String) -> Unit
) {

    val state = rememberLazyGridState()
    val context = LocalContext.current
    val selectedItem = remember { mutableStateOf(ScanWifiPages.SCAN_WIFI) }
    val transition = updateTransition(selectedItem.value, label = selectedItem.value.name)
    val serverSelected = remember { mutableStateOf<ServerInfo?>(null) }
    val scope = rememberCoroutineScope()
    val backAction = {
        scope.launch {
            selectedItem.value = ScanWifiPages.SCAN_WIFI
            delay(200)
            serverSelected.value = null
        }
    }

    BackHandler(selectedItem.value == ScanWifiPages.SERVER) {
        backAction()
    }

    transition.AnimatedContent {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                ScanWifiPages.SCAN_WIFI -> if (networkInfo.isNetworkAvailable.value && networkInfo.isWifiNetwork.value) {

                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (ipSearchData.ipList.list.isNotEmpty()) {

                            TextAuto(id = R.string.servers)

                            LazyVerticalGrid(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .weight(9f),
                                columns = GridCells.Adaptive(250.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                state = state
                            ) {
                                items(
                                    count = ipSearchData.ipList.size.value,
                                    key = { ipSearchData.ipList.list.elementAt(it).hashCode() }
                                ) {
                                    IPItem(
                                        IP = ipSearchData.ipList.list.elementAt(it).ip,
                                        url = "http://${ipSearchData.ipList.list.elementAt(it).ip}:$PORT",
                                        openUrl = openUrl
                                    ) {
                                        selectedItem.value = ScanWifiPages.SERVER
                                        serverSelected.value = ipSearchData.ipList.list.elementAt(it)
                                    }
                                }
                            }
                        }
                        else {
                            Image(
                                painter = painterResource(id = R.drawable.server_error_rafiki),
                                contentDescription = stringResource(id = R.string.no_server_found)
                            )

                            TextAuto(id = R.string.no_server_found)
                        }


                        if (ipSearchData.isSearching.value == 0)
                            Button(onClick = {
                                context.findServers(settings = settings)
                            }) {
                                TextAuto(
                                    id = R.string.scan_network_for_servers,
                                    interactable = true
                                )
                            }
                    }
                }
                ScanWifiPages.SERVER -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = stringResource(id = R.string.back),
                        ) {
                            backAction()
                        }

                        TextAuto(
                            text = serverSelected.value?.ip,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }

                    ServerUI(serverInfo = serverSelected.value)
                }
            }
        }
    }
}

enum class ScanWifiPages {
    SCAN_WIFI,
    SERVER
}