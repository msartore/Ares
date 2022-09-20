package dev.msartore.ares.ui.views

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dev.msartore.ares.MainActivity
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.FileItem
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
    val context = LocalContext.current

    if (serverInfo != null) {

        val loadData = {
            cor {
                swipeRefreshState.isRefreshing = true
                serverFiles.clear()
                serverInfoExtraction(serverInfo.ip)?.let { serverFiles.addAll(it) }
                swipeRefreshState.isRefreshing = false
            }
        }

        SwipeRefresh(
            modifier = Modifier.fillMaxSize(),
            state = swipeRefreshState,
            onRefresh = {
                if (!swipeRefreshState.isRefreshing) {
                    loadData()
                }
            }
        ) {
            LaunchedEffect(key1 = true) {
                loadData()
            }

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

                            Log.d("client", "downloading $name ${"http://${serverInfo.ip}:$PORT/${index}L"}")
                        }
                    }
                }
            }
        }
    }
}