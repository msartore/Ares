package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.server.KtorService.KtorServer.fileTransfer
import kotlinx.coroutines.cancel

@Composable
fun TransferDialog(
    status: MutableState<Boolean>,
    progress: MutableState<Float>
) {

    DialogContainer(status = status) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextAuto(
                text = "Transfer in progress"
            )

            if (progress.value > 0)
                LinearProgressIndicator(progress = progress.value)
            else
                LinearProgressIndicator()

            Icon(
                id = R.drawable.cancel_24px,
                contentDescription = stringResource(id = R.string.cancel)
            ) {
                fileTransfer.pipelineContext?.cancel()
            }
        }
    }
}