package dev.msartore.ares.ui.views

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.ExpandableCard
import dev.msartore.ares.ui.compose.FileItem
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.downloadFile
import dev.msartore.ares.utils.serverInfoExtraction
import dev.msartore.ares.utils.work
import dev.msartore.ares.viewmodels.MainViewModel
import dev.msartore.ares.viewmodels.ServerFinderViewModel

@ExperimentalGetImage
@Composable
fun ServerUI(
    serverInfo: ServerInfo?,
    mainViewModel: MainViewModel,
    serverFinderViewModel: ServerFinderViewModel,
) {
    val context = LocalContext.current

    serverFinderViewModel.apply {
        if (serverInfo != null) {

            val loadData = {
                job = work {
                    runCatching {
                        isRefreshing.value = true
                        error.value = false
                        serverFiles.clear()
                        serverInfoExtraction(
                            serverInfo.ip,
                            client = mainViewModel.client
                        )?.let { list ->
                            if (list.none { it.name == null })
                                serverFiles.addAll(list)
                            else
                                error.value = true
                        }
                        isRefreshing.value = false
                    }.onFailure {
                        error.value = true
                        isRefreshing.value = false
                    }
                }
            }

            LaunchedEffect(key1 = true) {

                if (isNewServer) {
                    isNewServer = false
                    loadData()
                }
            }

            LaunchedEffect(isRefreshing.value) {
                if (isRefreshing.value) {
                    // Infinite repeatable rotation when is playing
                    rotation.animateTo(
                        targetValue = currentRotation + 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    ) {
                        currentRotation = value
                    }
                } else {
                    if (currentRotation != 90f) {
                        // Slow down rotation on pause
                        rotation.animateTo(
                            targetValue = currentRotation + 50,
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = LinearOutSlowInEasing
                            )
                        ) {
                            currentRotation = value
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back_24px),
                        contentDescription = stringResource(id = R.string.back),
                    ) {
                        serverFinderViewModel.backToScanWifi()
                    }

                    TextAuto(
                        text = serverInfo.ip,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }

                Icon(
                    modifier = Modifier
                        .rotate(rotation.value),
                    painter = painterResource(id = R.drawable.refresh_24px),
                    contentDescription = stringResource(id = R.string.refresh),
                ) {
                    loadData()
                }
            }

            if (error.value)
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.server_error_rafiki),
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
                        serverFiles.elementAt(it).run {

                            val url = "http://${serverInfo.ip}:$PORT/$index"

                            ExpandableCard { expanded ->
                                FileItem(
                                    fileDataJson = this,
                                    onDownload = {
                                        mainViewModel.downloadManager?.downloadFile(
                                            url = url,
                                            mimeType = mimeType,
                                            fileName = "$name",
                                            context = context
                                        )
                                    },
                                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                                    onStreaming = {
                                        mainViewModel.openStreaming(
                                            context = context,
                                            url = "$url?streaming=true",
                                            fileType = fileType
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
        }
    }
}