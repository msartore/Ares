package dev.msartore.ares.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.models.FileType
import dev.msartore.ares.utils.printableSize

@Composable
fun FileItem(
    fileDataJson: FileDataJson,
    maxLines: Int = 1,
    onDownload: () -> Unit,
    onStreaming: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {

    fileDataJson.run {
        FileItem(
            name = name,
            text = text,
            size = size,
            icon = icon,
            maxLines = maxLines,
            content = {
                if (fileType != FileType.TEXT) {
                    if (fileDataJson.fileType == FileType.VIDEO || fileDataJson.fileType == FileType.IMAGE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            modifier = Modifier.size(40.dp),
                            id = R.drawable.open_in_new_24px
                        ) {
                            onStreaming()
                        }
                    }

                    Icon(
                        modifier = Modifier.size(40.dp),
                        id = R.drawable.file_download_48px
                    ) {
                        onDownload()
                    }
                }
                else {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        id = R.drawable.content_copy_24px
                    ) {
                        onCopy()
                    }

                    Icon(
                        modifier = Modifier.size(40.dp),
                        id = R.drawable.share_24px
                    ) {
                        onShare()
                    }
                }
            }
        )
    }
}

@Composable
fun FileItem(
    fileData: FileData,
    maxLines: Int = 1,
) {

    fileData.run {
        FileItem(
            name = name,
            text = text,
            size = size,
            icon = icon,
            maxLines = maxLines
        )
    }
}

@Composable
fun FileItem(
    maxLines: Int = 1,
    name: String? = null,
    text: String? = null,
    size: Int?,
    icon: Int?,
    content: @Composable (() -> Unit)? = null,
) {

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 8.dp),
                id = icon
            )

            Column {
                TextAuto(
                    text = text ?: "${stringResource(id = R.string.name)}: $name",
                    maxLines = maxLines
                )

                size?.let {
                    TextAuto(
                        text = "${stringResource(id = R.string.size)}: ${size.printableSize()}",
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            content?.invoke()
        }
    }
}