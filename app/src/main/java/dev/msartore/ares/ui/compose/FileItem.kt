package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.utils.printableSize


@Composable
fun FileItem(
    fileDataJson: FileDataJson,
    onClick: (() -> Unit)
) {
    fileDataJson.apply {
        FileItem(
            name = name,
            size = size,
            icon = icon,
            content = {
                Icon(
                    id = R.drawable.file_download_48px
                ) {
                    onClick()
                }
            }
        )
    }
}

@Composable
fun FileItem(
    fileData: FileData
) {
    fileData.apply {
        FileItem(
            modifier = Modifier.background(
                color = if (selected.value)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            ),
            name = name,
            size = size,
            icon = icon
        ) {
            selected.value = !selected.value
        }
    }
}

@Composable
fun FileItem(
    modifier: Modifier = Modifier,
    name: String?,
    size: Int?,
    icon: Int?,
    content: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {

    Row(
        modifier = modifier
            .height(120.dp)
            .width(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick?.invoke()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(9F, false),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 8.dp),
                id = icon
            )

            Column {
                TextAuto(text = "${stringResource(id = R.string.name)}: $name")

                TextAuto(text = "${stringResource(id = R.string.size)}: ${(size?:1).printableSize()}")
            }
        }

        Row(
            modifier = Modifier.weight(2F, false),
        ) {
            content?.invoke()
        }
    }
}