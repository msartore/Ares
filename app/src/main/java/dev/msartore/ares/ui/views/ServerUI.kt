package dev.msartore.ares.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dev.msartore.ares.MainActivity
import dev.msartore.ares.R
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.FileItem
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.cor
import dev.msartore.ares.utils.downloadFile
import dev.msartore.ares.utils.serverInfoExtraction
import dev.msartore.ares.utils.work

@Composable
fun ServerUI(
    serverInfo: ServerInfo?
) {

    val serverFiles = remember { mutableStateListOf<FileDataJson>() }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = true)
    val state = rememberLazyGridState()
    val error = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (serverInfo != null) {

        val loadData = {
            cor {
                error.value = false
                swipeRefreshState.isRefreshing = true
                serverFiles.clear()
                serverInfoExtraction(serverInfo.ip)?.let { list ->
                    if (list.none { it.name == null })
                        serverFiles.addAll(list)
                    else
                        error.value = true
                }
                swipeRefreshState.isRefreshing = false
            }
        }

        LaunchedEffect(key1 = true) {
            loadData()
        }

        SwipeRefresh(
            modifier = Modifier.fillMaxHeight(),
            state = swipeRefreshState,
            onRefresh = {
                if (!swipeRefreshState.isRefreshing) {
                    loadData()
                }
            }
        ) {

            if (error.value)
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.computer_troubleshooting_rafiki),
                        contentDescription = stringResource(id = R.string.error_during_connection)
                    )

                    TextAuto(id = R.string.error_during_connection_try_again)

                    Button(onClick = { loadData() }) {
                        TextAuto(id = R.string.refresh)
                    }
                }
            else
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxHeight(),
                    columns = GridCells.Adaptive(250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    state = state
                ) {
                    items(
                        count = serverFiles.size,
                        key = { serverFiles.elementAt(it).index.hashCode() }
                    ) {
                        serverFiles.elementAt(it).apply {

                            FileItem(this) {
                                work {
                                    MainActivity.MActivity.downloadManager?.downloadFile(
                                        url = "http://${serverInfo.ip}:$PORT/${index}",
                                        mimeType = mimeType,
                                        fileName = "$name",
                                        context = context
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}