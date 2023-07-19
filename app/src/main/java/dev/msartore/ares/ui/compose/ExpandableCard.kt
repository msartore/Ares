package dev.msartore.ares.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    content: @Composable (Boolean) -> Unit
) {
    val expandedState = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = {
                expandedState.value = !expandedState.value
            }, onLongClick = {
                onLongClick?.invoke()
            }),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = modifier.padding(12.dp)
        ) {
            content(expandedState.value)
        }
    }
}