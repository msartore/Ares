package dev.msartore.ares.ui.views

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.compose.ExpandableCard
import dev.msartore.ares.ui.compose.FileItem
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.downloadFile
import dev.msartore.ares.utils.packageInfo
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
    val lowerVersion = remember { mutableStateOf(false) }
    val expanded = remember { mutableStateOf(false) }

    serverFinderViewModel.run {
        if (serverInfo != null) {
            val loadData = {
                job = work {
                    runCatching {
                        isRefreshing.value = true
                        error.value = false
                        serverFiles.clear()
                        serverInfoExtraction(
                            serverInfo.ip, client = mainViewModel.client
                        )?.let { (version, list) ->
                            lowerVersion.value = (version.filter { it.isDigit() }.toIntOrNull()
                                ?: 0) < (context.packageInfo().versionName.filter { it.isDigit() }
                                .toIntOrNull() ?: 0)

                            if (list.none { it.UUID == null }) serverFiles.addAll(list)
                            else error.value = true
                        }
                        isRefreshing.value = false
                    }.onFailure {
                        it.printStackTrace()
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
                        targetValue = currentRotation + 360f, animationSpec = infiniteRepeatable(
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
                            targetValue = currentRotation + 50, animationSpec = tween(
                                durationMillis = 250, easing = LinearOutSlowInEasing
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
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!error.value && !isRefreshing.value && !serverFiles.isEmpty()) {
                        Box {
                            Icon(
                                id = R.drawable.more_vert_24px,
                                contentDescription = stringResource(id = androidx.compose.ui.R.string.dropdown_menu),
                            ) {
                                expanded.value = true
                            }

                            DropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false }) {

                                    DropdownMenuItem(
                                        text = { TextAuto(id = R.string.download_all_zip) },
                                        onClick = {
                                            work {
                                                mainViewModel.run {
                                                    downloadManager?.downloadFile(
                                                        url = "http://${serverInfo.ip}:$port/download_all",
                                                        mimeType = "application/zip",
                                                        fileName = "download_all.zip",
                                                        context = context
                                                    )
                                                }
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                id = R.drawable.folder_zip_24px,
                                                contentDescription = stringResource(id = R.string.download_all_zip)
                                            )
                                        })

                                    DropdownMenuItem(
                                        text = { TextAuto(id = R.string.download_all) },
                                        onClick = {
                                            work {
                                                serverFiles.forEach {
                                                    it.run {
                                                        mainViewModel.run {
                                                            downloadManager?.downloadFile(
                                                                url = "http://${serverInfo.ip}:$port/$UUID",
                                                                mimeType = mimeType,
                                                                fileName = "$name",
                                                                context = context
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                id = R.drawable.download_24px,
                                                contentDescription = stringResource(id = R.string.download_all)
                                            )
                                        })

                                }
                            }
                    }

                    Icon(
                        modifier = Modifier
                            .rotate(rotation.value)
                            .size(40.dp),
                        painter = painterResource(id = R.drawable.refresh_24px),
                        contentDescription = stringResource(id = R.string.refresh),
                    ) {
                        loadData()
                    }
                }
            }

            if (error.value) Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id =
                        if (lowerVersion.value) R.drawable.update_rafiki
                        else R.drawable.server_error_rafiki
                    ),
                    contentDescription = stringResource(id =
                        if (lowerVersion.value) R.string.error_update_server
                        else R.string.error_during_connection
                    )
                )

                TextAuto(id =
                    if (lowerVersion.value) R.string.error_update_server_message
                    else R.string.error_during_connection_try_again)

                Button(onClick = { loadData() }) {
                    TextAuto(id = R.string.refresh)
                }
            }
            else if(serverFiles.isEmpty() && !isRefreshing.value) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextAuto(id = R.string.no_file_available)
                }
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxHeight(),
                    columns = GridCells.Adaptive(250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    state = state
                ) {
                    items(
                        count = serverFiles.size,
                        key = { serverFiles.elementAt(it).UUID.hashCode() }) {
                        serverFiles.elementAt(it).run {
                            val url = "http://${serverInfo.ip}:$port/$UUID"

                            ExpandableCard(
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                            ) { expanded ->
                                FileItem(
                                    fileDataJson = this,
                                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                                    onDownload = {
                                        mainViewModel.run {
                                            downloadManager?.downloadFile(
                                                url = url,
                                                mimeType = mimeType,
                                                fileName = "$name",
                                                context = context
                                            )
                                        }
                                    },
                                    onStreaming = {
                                        mainViewModel.openStreaming(
                                            context = context,
                                            url = "$url?streaming=true",
                                            fileType = fileType
                                        )
                                    },
                                    onShare = {
                                        mainViewModel.run {
                                            context.shareText("$text")
                                        }
                                    },
                                    onCopy = {
                                        mainViewModel.copyText(
                                            context.getString(R.string.text_input), "$text"
                                        )
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}