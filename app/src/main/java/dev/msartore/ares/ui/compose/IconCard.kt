package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IconCard(
    modifier: Modifier = Modifier,
    id: Int,
    contentDescription: String,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onClick: () -> Unit
) {
    Icon(
        modifier = modifier
            .background(color, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick.invoke() }
            .padding(8.dp),
        id = id,
        contentDescription = contentDescription
    )
}