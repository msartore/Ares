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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.FileTransfer
import kotlinx.coroutines.cancel

@Composable
fun TransferDialog(
    fileTransfer: FileTransfer,
) {
    fileTransfer.run {
        DialogContainer(status = isActive) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextAuto(id = if (file == null) R.string.transfer_in_progress else R.string.compression_in_progress)

                if (sizeTransferred.value > 0) LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(), progress = sizeTransferred.value
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
                        }.getOrElse {
                            it.printStackTrace()
                        }
                    }) {
                        TextAuto(id = R.string.cancel)
                    }
                }
            }
        }
    }
}