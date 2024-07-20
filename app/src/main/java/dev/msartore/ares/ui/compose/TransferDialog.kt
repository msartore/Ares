package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.FileTransfer
import dev.msartore.ares.models.FileTransferStages
import kotlinx.coroutines.cancel

@Composable
fun TransferDialog(
    fileTransfer: FileTransfer,
) {
    fileTransfer.run {
        val statusDialog = remember { mutableStateOf(true) }

        LaunchedEffect(key1 = status.value) {
            statusDialog.value = status.value != FileTransferStages.INACTIVE
        }

        DialogContainer(status = statusDialog) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextAuto(id = when(status.value) {
                    FileTransferStages.INITIALIZING -> R.string.initializing
                    FileTransferStages.ARCHIVING -> R.string.archiving_in_progress
                    FileTransferStages.TRANSMITTING -> R.string.transmission_in_progress
                    else -> R.string.finalizing
                })

                if (sizeTransferred.value > 0) LinearProgressIndicator(
                    progress = {
                        sizeTransferred.value
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                else LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                TextAuto(text = name.value)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        runCatching {
                            fileTransfer.pipelineContext?.cancel()
                            fileTransfer.cancelled = true
                        }.onFailure {
                            it.printStackTrace()
                        }

                        fileTransfer.run {
                            if (file?.exists() == true) file!!.delete()
                        }

                        status.value = FileTransferStages.INACTIVE
                    }) {
                        TextAuto(id = R.string.cancel)
                    }
                }
            }
        }
    }
}