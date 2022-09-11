package dev.msartore.ares.ui.compose.basic

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

@Composable
fun FileItem(
    fileData: FileData
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .width(250.dp)
            .background(
                color = if (fileData.selected.value)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                fileData.selected.value = !fileData.selected.value
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(50.dp)
                .padding(end = 8.dp),
            id = fileData.icon
        )

        Column {
            TextAuto(text = "${stringResource(id = R.string.name)}: ${fileData.name}")

            TextAuto(text = "${stringResource(id = R.string.size)}: ${"%.2f".format((fileData.size ?: 1)/1000000.0)}MB")
        }
    }
}