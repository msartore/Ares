package dev.msartore.ares.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.msartore.ares.MainActivity.MActivity.networkInfo
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.server.KtorService.KtorServer.isServerOn
import dev.msartore.ares.ui.compose.FileItem
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.isWideView
import kotlinx.coroutines.launch

@Composable
fun HomeUI(
    maxWidth: Dp?,
    isLoading: MutableState<Boolean>,
    onImportFilesClick: () -> Unit,
    onStartServerClick: () -> Unit,
    onStopServerClick: () -> Unit
) {

    val state = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val homeUIContent1: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                TextAuto(
                    id = R.string.server,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    TextAuto(text = "${stringResource(id = R.string.running)}: ")

                    TextAuto(
                        text = isServerOn.value.toString(),
                        color = if (isServerOn.value) Color.Green else Color.Red
                    )
                }


                networkInfo.apply {
                    TextAuto(
                        text =
                        when {
                            isNetworkAvailable.value && isWifiNetwork.value ->
                                "${stringResource(id = R.string.ip_address)}:" +
                                        " ${ipAddress.value}" +
                                        if (isServerOn.value) ":${PORT}" else ""
                            !isWifiNetwork.value && isNetworkAvailable.value ->
                                stringResource(id = R.string.wrong_network)
                            else ->
                                stringResource(id = R.string.no_network_available)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isServerOn.value) {
                        TextButton(onClick = {
                            if (isServerOn.value)
                                onStopServerClick()
                        }) {
                            TextAuto(
                                id = R.string.stop_server,
                                interactable = true
                            )
                        }
                    }
                    else {
                        networkInfo.apply {
                            if (isNetworkAvailable.value && isWifiNetwork.value)
                                TextButton(onClick = { onStartServerClick() }) {
                                    TextAuto(
                                        id = R.string.start_server,
                                        interactable = true
                                    )
                                }
                        }
                    }
                }
            }

            Divider()

            Column {
                TextAuto(
                    id = R.string.file,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextAuto(text = "${stringResource(id = R.string.available)}: ${concurrentMutableList.size.value}" )

                TextAuto(text = "${stringResource(id = R.string.selected)}: ${concurrentMutableList.list.filter { it.selected.value }.size}")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading.value)
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .weight(1f, false),
                            strokeWidth = 2.dp
                        )

                    Row {
                        if (concurrentMutableList.list.any { it.selected.value })
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        concurrentMutableList.removeIf { it.selected.value }
                                    }
                                }
                            ) {
                                TextAuto(
                                    id = R.string.delete_selected,
                                    textAlign = TextAlign.Center,
                                    interactable = true
                                )
                            }
                        if (concurrentMutableList.size.value > 0) {
                            val allSelected =concurrentMutableList.list.all { it.selected.value }

                            TextButton(
                                onClick = {
                                    scope.launch {
                                        if (allSelected)
                                            concurrentMutableList.list.forEach { it.selected.value = false }
                                        else
                                            concurrentMutableList.list.forEach { it.selected.value = true }
                                    }
                                }
                            ) {
                                TextAuto(
                                    id =
                                    if (allSelected)
                                        R.string.unselect_all
                                    else
                                        R.string.select_all,
                                    textAlign = TextAlign.Center,
                                    interactable = true
                                )
                            }
                        }
                        TextButton(
                            onClick = { onImportFilesClick() }
                        ) {
                            TextAuto(
                                id = R.string.import_files,
                                textAlign = TextAlign.Center,
                                interactable = true
                            )
                        }
                    }
                }
            }
        }
    }
    val homeUIContent2: @Composable (Modifier) -> Unit = { modifier ->
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Adaptive(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = state
        ) {
            items(
                count = concurrentMutableList.size.value,
                key = { concurrentMutableList.list.elementAt(it).uri }
            ) {
                FileItem(concurrentMutableList.list.elementAt(it))
            }
        }
    }

    if(maxWidth?.isWideView() == true)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            homeUIContent1(Modifier.weight(1f))
            homeUIContent2(Modifier.weight(1f))
        }
    else
        Column {
            homeUIContent1(Modifier)
            homeUIContent2(Modifier)
        }
}