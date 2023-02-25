package dev.msartore.ares.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LicenseItem(
    titleId: Int,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(35.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        TextAuto(
            id = titleId,
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        TextAuto(
            text = text,
            maxLines = Int.MAX_VALUE
        )
    }
}