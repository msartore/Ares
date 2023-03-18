package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardIcon(
    iconId: Int, textId: Int, contentDescription: String, onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(100.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick()
            }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            id = iconId, contentDescription = contentDescription
        )

        TextAuto(
            modifier = Modifier.fillMaxWidth(),
            id = textId,
            textAlign = TextAlign.End,
            maxLines = 3,
            fontSize = 12.sp
        )
    }
}