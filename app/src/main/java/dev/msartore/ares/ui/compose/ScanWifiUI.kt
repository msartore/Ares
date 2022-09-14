package dev.msartore.ares.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.MainActivity.MActivity.ipSearchData
import dev.msartore.ares.MainActivity.MActivity.networkInfo
import dev.msartore.ares.R
import dev.msartore.ares.models.KtorService.KtorServer.PORT
import dev.msartore.ares.models.Settings
import dev.msartore.ares.ui.compose.basic.IPItem
import dev.msartore.ares.utils.findServers
import dev.msartore.ares.ui.compose.basic.Icon
import dev.msartore.ares.ui.compose.basic.TextAuto
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

@Composable
fun ScanWifiUI(
    settings: Settings,
    openUrl: (String) -> Unit
) {

    val state = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (networkInfo.isNetworkAvailable.value && networkInfo.isWifiNetwork.value) {

            if (ipSearchData.isSearching.value != 0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, false)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        TextAuto(id = R.string.scanning_servers)

                        TextAuto(text = "${stringResource(id = R.string.ip_left_check)}: ${ipSearchData.ipLeft.value}")
                    }

                    Icon(
                        id = R.drawable.cancel_24px
                    ) {
                        scope.launch {
                            ipSearchData.job.cancelAndJoin()
                            ipSearchData.isSearching.value = 0
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(5f)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceAround,
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
                                    IP = ipSearchData.ipList.list.elementAt(it),
                                    url = "http://${ipSearchData.ipList.list.elementAt(it)}:$PORT",
                                    openUrl = openUrl
                                )
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
                            TextAuto(id = R.string.scan_network_for_servers)
                        }
                }
        }
    }
}