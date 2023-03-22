package dev.msartore.ares.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.FileDownload


@Composable
fun SnackBar(
    modifier: Modifier = Modifier, visible: Boolean, content: @Composable (() -> Unit)
) {
    AnimatedVisibility(modifier = modifier,
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it + it / 2 }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(false) { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun SnackBarDownload(
    modifier: Modifier = Modifier,
    fileDownload: FileDownload?,
    onOpenFile: (() -> Unit)?,
    onShareFile: (() -> Unit)?
) {
    SnackBar(
        modifier = modifier,
        visible = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextAuto(
                modifier = Modifier.weight(5f),
                text = fileDownload?.fileData?.name,
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
                    onOpenFile?.invoke()
                }

                Icon(
                    id = R.drawable.share_24px
                ) {
                    onShareFile?.invoke()
                }
            }
        }
    }
}