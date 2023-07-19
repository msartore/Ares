package dev.msartore.ares.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.*
import dev.msartore.ares.R
import dev.msartore.ares.models.TransferFile
import dev.msartore.ares.models.TransferFileType
import dev.msartore.ares.ui.compose.Icon
import dev.msartore.ares.ui.compose.TextAuto
import dev.msartore.ares.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferUI(
    mainViewModel: MainViewModel = viewModel()
) {
    val state = rememberLazyListState()
    val transferFilterState = remember { mutableStateOf(TransferFilter.ALL) }
    val list = remember { mutableStateListOf<TransferFile>() }

    if (mainViewModel.transferredFiles.any()) {
        Column(
            modifier = Modifier.padding(top = 16.dp)
        ) {

            TextAuto(id = R.string.filter_by)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransferFilter.values().forEach {
                    FilterChip(selected = transferFilterState.value == it, onClick = { transferFilterState.value = it }, label = { TextAuto(id = it.id) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LaunchedEffect(key1 = transferFilterState.value) {
                list.clear()

                when(transferFilterState.value) {
                    TransferFilter.ALL -> {
                        list.addAll(mainViewModel.transferredFiles)
                    }
                    TransferFilter.DOWNLOAD -> {
                        list.addAll(mainViewModel.transferredFiles.filter { it.transferStages == TransferFileType.DOWNLOAD })
                    }
                    TransferFilter.UPLOAD -> {
                        list.addAll(mainViewModel.transferredFiles.filter { it.transferStages == TransferFileType.UPLOAD })
                    }
                }
            }

            LaunchedEffect(key1 = mainViewModel.transferredFiles.size) {
                list.clear()
                list.addAll(mainViewModel.transferredFiles)
            }

            LazyColumn(state = state) {
                itemsIndexed(
                    items = list,
                    key = { _, transferFileData -> transferFileData.fileData.uuid }
                ) { _, transferFileData ->

                    LaunchedEffect(key1 = true) {
                        transferFileData.viewed.value = true
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextAuto(
                            modifier = Modifier.weight(5f),
                            text = transferFileData.fileData.name,
                            maxLines = 1
                        )

                        Row(
                            modifier = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                id = R.drawable.open_in_new_24px
                            ) {
                                mainViewModel.onOpenFile?.invoke(transferFileData.fileData)
                            }

                            Icon(
                                id = R.drawable.share_24px
                            ) {
                                mainViewModel.onShareFile?.invoke(transferFileData.fileData)
                            }
                        }
                    }
                }
            }
        }
    }
    else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextAuto(id = R.string.no_file_available, fontWeight = FontWeight.Bold)
        }
    }
}

enum class TransferFilter(val id: Int) {
    ALL(R.string.all),
    DOWNLOAD(R.string.download),
    UPLOAD(R.string.upload)
}