package dev.msartore.ares.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.MainActivity.MActivity.networkInfo
import dev.msartore.ares.R
import dev.msartore.ares.models.KtorService.KtorServer.PORT
import dev.msartore.ares.models.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.models.KtorService.KtorServer.isServerOn
import dev.msartore.ares.ui.compose.basic.FileItem
import dev.msartore.ares.ui.compose.basic.TextAuto
import kotlinx.coroutines.launch

@Composable
fun HomeUI(
    isLoading: MutableState<Boolean>,
    onImportFilesClick: () -> Unit,
    onStartServerClick: () -> Unit,
    onStopServerClick: () -> Unit
) {

    val state = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Column {
        TextAuto(id = R.string.server)

        TextAuto(text = "${stringResource(id = R.string.running)}: ${isServerOn.value}")

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
                    TextAuto(id = R.string.stop_server)
                }
            }
            else {
                networkInfo.apply {
                    if (isNetworkAvailable.value && isWifiNetwork.value)
                        TextButton(onClick = { onStartServerClick() }) {
                            TextAuto(id = R.string.start_server)
                        }
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(vertical = 16.dp)
    )

    Column {
        TextAuto(id = R.string.file)

        TextAuto(text = "${stringResource(id = R.string.available)}: ${concurrentMutableList.size.value}" )

        TextAuto(text = "${stringResource(id = R.string.selected)}: ${concurrentMutableList.list.filter { it.selected.value }.size}")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading.value)
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (concurrentMutableList.list.any { it.selected.value })
                    TextButton(onClick = {
                        scope.launch {
                            concurrentMutableList.removeIf { it.selected.value }
                        }
                    }) {
                        TextAuto(text = stringResource(id = R.string.delete_selected))
                    }

                if (concurrentMutableList.size.value > 0) {
                    val allSelected =concurrentMutableList.list.all { it.selected.value }

                    TextButton(onClick = {
                        scope.launch {
                            if (allSelected)
                                concurrentMutableList.list.forEach { it.selected.value = false }
                            else
                                concurrentMutableList.list.forEach { it.selected.value = true }
                        }
                    }) {
                        TextAuto(
                            id =
                            if (allSelected)
                                R.string.unselect_all
                            else
                                R.string.select_all
                        )
                    }
                }

                TextButton(onClick = { onImportFilesClick() }) {
                    TextAuto(id = R.string.import_files)
                }
            }
        }
    }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxHeight(),
        columns = GridCells.Adaptive(250.dp),
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