package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R

@Composable
fun ServerItem(
    ip: String, url: String, openUrl: (String) -> Unit, serverSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .width(250.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                serverSelected()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .weight(1f)
                .padding(end = 8.dp), id = R.drawable.hdd_network
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextAuto(text = ip)

            IconCard(
                modifier = Modifier.size(35.dp),
                id = R.drawable.north_east_24px,
                contentDescription = stringResource(id = R.string.open_in_browser),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                openUrl(url)
            }
        }
    }
}