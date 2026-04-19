package dev.msartore.ares.ui.components.views

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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.server.ServerInfo
import dev.msartore.ares.ui.components.ExpandableCard
import dev.msartore.ares.ui.components.FileItem
import dev.msartore.ares.ui.components.Icon
import dev.msartore.ares.ui.components.TextAuto
import dev.msartore.ares.ui.screens.main.MainEvent
import dev.msartore.ares.ui.screens.main.MainViewModel
import dev.msartore.ares.ui.screens.serverFinder.ServerFinderEvent
import dev.msartore.ares.ui.screens.serverFinder.ServerFinderState
import dev.msartore.ares.utils.downloadFile
import dev.msartore.ares.utils.packageInfo
import dev.msartore.ares.utils.serverInfoExtraction
import dev.msartore.ares.utils.work

@Composable
fun ServerUI(
    serverInfo: ServerInfo?,
    mainViewModel: MainViewModel,
    state: ServerFinderState,
    onEvent: (ServerFinderEvent) -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val gridState = rememberLazyGridState()
    var expanded by remember { mutableStateOf(false) }
    var lowerVersion by remember { mutableStateOf(false) }

    if (serverInfo != null) {
        LaunchedEffect(Unit) {
            work {
                runCatching {
                    serverInfoExtraction(serverInfo.ip, client = mainViewModel.client)?.let { (version, list) ->
                        lowerVersion = (version.filter { it.isDigit() }.toIntOrNull() ?: 0) < 
                            (context.packageInfo().versionName?.filter { it.isDigit() }?.toIntOrNull() ?: 0)
                        onEvent(ServerFinderEvent.FilesLoaded(list))
                    }
                }.onFailure {
                    onEvent(ServerFinderEvent.FilesLoadFailed)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        onEvent(ServerFinderEvent.BackToServerList)
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
                    if (!state.error && !state.isRefreshing && state.serverFiles.isNotEmpty()) {
                        Box {
                            Icon(
                                id = R.drawable.more_vert_24px,
                                contentDescription = stringResource(id = R.string.dropdown_menu),
                            ) {
                                expanded = true
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { TextAuto(id = R.string.refresh) },
                                    onClick = {
                                        onEvent(ServerFinderEvent.RefreshFiles)
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            id = R.drawable.refresh_24px,
                                            contentDescription = stringResource(id = R.string.refresh),
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (state.isRefreshing) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.refresh_24px),
                            contentDescription = stringResource(id = R.string.refresh)
                        )
                    }

                    Icon(
                        id = R.drawable.refresh_24px,
                        contentDescription = stringResource(id = R.string.refresh),
                    ) {
                        onEvent(ServerFinderEvent.RefreshFiles)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                if (state.error) {
                    TextAuto(
                        id = R.string.error,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (state.serverFiles.isEmpty() && !state.isRefreshing) {
                    TextAuto(
                        id = R.string.no_file_available,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxHeight(),
                        columns = GridCells.Adaptive(250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = gridState
                    ) {
                        items(state.serverFiles) { file ->
                            val url = "http://${serverInfo.ip}:${port}/${file.uuid}"

                            ExpandableCard(
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                            ) { expanded ->
                                FileItem(
                                    fileDataJson = file,
                                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                                    onDownload = {
                                        mainViewModel.downloadManager.downloadFile(
                                            url = url,
                                            fileName = file.name ?: "file",
                                            mimeType = file.mimeType,
                                            context = context
                                        )
                                    },
                                    onStreaming = {
                                        mainViewModel.openStreaming(
                                            "http://${serverInfo.ip}:${port}/${file.uuid}",
                                            file.fileType
                                        )
                                    },
                                    onShare = {
                                        mainViewModel.onEvent(MainEvent.ShareTextRequested("http://${serverInfo.ip}:${port}/${file.uuid}"))
                                    },
                                    onCopy = {
                                        mainViewModel.onEvent(MainEvent.CopyTextRequested("URL", "http://${serverInfo.ip}:${port}/${file.uuid}"))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}