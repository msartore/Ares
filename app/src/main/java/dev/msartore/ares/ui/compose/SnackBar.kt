package dev.msartore.ares.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SnackBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(false) {  }
                .padding(16.dp),
        ) {
            content()
        }
    }
}