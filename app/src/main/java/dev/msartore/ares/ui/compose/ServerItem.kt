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
import androidx.compose.ui.unit.dp
import dev.msartore.ares.R

@Composable
fun ServerItem(
    IP: String, url: String, openUrl: (String) -> Unit, serverSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .width(250.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
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
                .size(50.dp)
                .padding(end = 8.dp), id = R.drawable.devices_24px
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextAuto(text = IP)

            Icon(id = R.drawable.open_in_browser_24px) {
                openUrl(url)
            }
        }
    }
}