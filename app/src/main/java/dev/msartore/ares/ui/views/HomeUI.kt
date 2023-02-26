package dev.msartore.ares.ui.views

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileType
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.server.KtorService.KtorServer.isServerOn
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.ui.compose.DialogContainer
import dev.msartore.ares.ui.compose.ExpandableCard
import dev.msartore.ares.ui.compose.FileItem
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.utils.isWideView
import dev.msartore.ares.viewmodels.HomeViewModel
import dev.msartore.ares.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
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
    val expanded = remember { mutableStateOf(false) }
    val homeUIContent1: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier
                .verticalScroll(home1State),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextAuto(
                id = R.string.ares,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
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
                            mainViewModel.run {
                                context.shareText("http://${networkInfo.ipAddress.value}:$port")
                            }
                        }

                        Icon(
                            id = R.drawable.stop_circle_24px,
                            contentDescription = stringResource(id = R.string.stop_server),
                        ) {
                            if (isServerOn.value)
                                homeViewModel.onStopServerClick()
                        }
                    }
                    else {
                        mainViewModel.networkInfo.run {
                            if (isNetworkAvailable.value && (isWifiNetwork.value || mainViewModel.settings?.removeWifiRestriction?.value == true))
                                Icon(
                                    id = R.drawable.power_rounded_24px,
                                    contentDescription = stringResource(id = R.string.start_server),
                                ) {
                                    homeViewModel.onStartServerClick()
                                }
                        }
                    }
                }
            }

            Column(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                TextAuto(
                    id = R.string.file,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextAuto(text = "${stringResource(id = R.string.available)}: ${concurrentMutableList.size.value}" )

                TextAuto(text = "${stringResource(id = R.string.selected)}: ${concurrentMutableList.list.filter { it.selected.value }.size}")

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading.value)
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .weight(1f, false),
                                strokeWidth = 2.dp
                            )

                        if (concurrentMutableList.list.any { it.selected.value })
                            Icon(
                                id = R.drawable.delete_24px,
                                contentDescription = stringResource(id = R.string.delete_selected),
                            ) {
                                scope.launch {
                                    concurrentMutableList.removeIf { it.selected.value }
                                }
                            }

                        if (concurrentMutableList.size.value > 0) {
                            val countSelected = concurrentMutableList.list.count { it.selected.value }

                            Icon(
                                id =
                                when (countSelected) {
                                    concurrentMutableList.size.value -> R.drawable.check_box_24px
                                    in 1..concurrentMutableList.size.value -> R.drawable.indeterminate_check_box_24px
                                    else -> R.drawable.check_box_outline_blank_24px
                                },
                                contentDescription = stringResource(id = R.string.select_all),
                            ) {
                                scope.launch {
                                    when (countSelected) {
                                        0 -> concurrentMutableList.list.forEach { it.selected.value = true }
                                        else -> concurrentMutableList.list.forEach { it.selected.value = false }
                                    }
                                }
                            }
                        }

                        Box {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Localized description") {
                                expanded.value = true
                            }

                            DropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { TextAuto(id = R.string.import_files) },
                                    onClick = {
                                        homeViewModel.onImportFilesClick()
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            id = R.drawable.upload_file_24px,
                                            contentDescription = stringResource(id = R.string.import_files),
                                        )
                                    })

                                DropdownMenuItem(
                                    text = { TextAuto(id = R.string.text_input) },
                                    onClick = {
                                        homeViewModel.dialogInput.value = true
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            id = R.drawable.title_24px,
                                            contentDescription = stringResource(id = R.string.text_input),
                                        )
                                    })
                            }
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
                key = {
                    concurrentMutableList.list.elementAt(it).UUID
                }
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
            homeUIContent1(
                Modifier
                    .fillMaxHeight()
                    .weight(1f))
            homeUIContent2(Modifier.weight(1f))
        }
    else
        Column(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            homeUIContent1(Modifier.wrapContentHeight())
            homeUIContent2(Modifier)
        }

    homeViewModel.run {
        DialogContainer(status = dialogInput) {
            val keyboardController = LocalSoftwareKeyboardController.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextAuto(
                    id = R.string.text_input
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = inputText.value,
                    onValueChange = { inputText.value = it },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    maxLines = 1
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            dialogInput.value = false
                            inputText.value = ""
                        }
                    ) {
                        TextAuto(id = R.string.cancel)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (inputText.value.isNotBlank())
                                concurrentMutableList.add(FileData(
                                    text = inputText.value,
                                    fileType = FileType.TEXT
                                ))

                            dialogInput.value = false
                            inputText.value = ""
                        }
                    ) {
                        TextAuto(id = R.string.save)
                    }
                }
            }
        }
    }
}