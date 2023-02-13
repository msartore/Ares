package dev.msartore.ares.ui.views

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.server.KtorService.KtorServer.isServerOn
import dev.msartore.ares.ui.compose.ExpandableCard
import dev.msartore.ares.ui.compose.FileItem
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.isWideView
import dev.msartore.ares.viewmodels.HomeViewModel
import dev.msartore.ares.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@ExperimentalGetImage
@Composable
fun HomeUI(
    maxWidth: Dp,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel = viewModel()
) {
    val lazyGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isLoading = homeViewModel.isLoading.collectAsState()
    val home1State = rememberScrollState()

    val homeUIContent1: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier.verticalScroll(home1State),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    id = R.drawable.logo,
                )
                TextAuto(
                    id = R.string.ares,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextAuto(
                            id = R.string.server,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (isServerOn.value)
                            Column {
                                if (mainViewModel.networkInfo.bitmap.value != null)
                                    Image(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onBackground,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                mainViewModel.qrCodeDialog.value = true
                                            },
                                        bitmap = mainViewModel.networkInfo.bitmap.value!!,
                                        contentDescription = "ip"
                                    )
                                else
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(50.dp),
                                    )
                            }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        TextAuto(text = "${stringResource(id = R.string.running)}: ")

                        TextAuto(
                            text = isServerOn.value.toString(),
                            color = if (isServerOn.value) Color.Green else Color.Red
                        )
                    }

                    mainViewModel.networkInfo.run {
                        TextAuto(
                            text =
                            when {
                                isNetworkAvailable.value && (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true) ->
                                    "${stringResource(id = R.string.ip_address)}:" +
                                            " ${ipAddress.value}" +
                                            if (isServerOn.value) ":${port}" else ""
                                !(isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true) && isNetworkAvailable.value ->
                                    stringResource(id = R.string.wrong_network)
                                else ->
                                    stringResource(id = R.string.no_network_available)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        if (isServerOn.value) Arrangement.SpaceBetween
                        else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (isServerOn.value) {

                        Icon(
                            id = R.drawable.share_24px
                        ) {
                            mainViewModel.apply {
                                context.shareText("http://${networkInfo.ipAddress.value}:$port")
                            }
                        }

                        TextButton(onClick = {
                            if (isServerOn.value)
                                homeViewModel.onStopServerClick()
                        }) {
                            TextAuto(
                                id = R.string.stop_server,
                            )
                        }
                    }
                    else {
                        mainViewModel.networkInfo.run {
                            if (isNetworkAvailable.value && (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true))
                                TextButton(onClick = { homeViewModel.onStartServerClick() }) {
                                    TextAuto(
                                        id = R.string.start_server,
                                    )
                                }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                TextAuto(
                    id = R.string.file,
                    style = MaterialTheme.typography.titleMedium
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
                                modifier = Modifier.weight(1f, false),
                                onClick = {
                                    scope.launch {
                                        concurrentMutableList.removeIf { it.selected.value }
                                    }
                                }
                            ) {
                                TextAuto(
                                    id = R.string.delete_selected,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        if (concurrentMutableList.size.value > 0) {
                            val allSelected =concurrentMutableList.list.all { it.selected.value }

                            TextButton(
                                modifier = Modifier.weight(1f, false),
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
                                )
                            }
                        }
                        TextButton(
                            modifier = Modifier.weight(1f, false),
                            onClick = {
                                homeViewModel.onImportFilesClick()
                            }
                        ) {
                            TextAuto(
                                id = R.string.import_files,
                                textAlign = TextAlign.Center,
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
            state = lazyGridState
        ) {
            items(
                count = concurrentMutableList.size.value,
                key = { concurrentMutableList.list.elementAt(it).uri }
            ) { index ->

                concurrentMutableList.list.elementAt(index).run {
                    ExpandableCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected.value) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer),
                        onLongClick = {
                            selected.value = !selected.value
                        }
                    ) {
                        FileItem(
                            fileData = this,
                            maxLines = if (it) Int.MAX_VALUE else 1
                        )
                    }
                }
            }
        }
    }

    if (maxWidth.isWideView())
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            homeUIContent1(Modifier.weight(1f))
            homeUIContent2(Modifier.weight(1f))
        }
    else
        Column(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            homeUIContent1(Modifier)
            homeUIContent2(Modifier)
        }
}