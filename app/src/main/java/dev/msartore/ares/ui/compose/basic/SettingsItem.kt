package dev.msartore.ares.ui.compose.basic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    title: String,
    icon: Painter,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {

    val modifier =
        if (onClick != null)
            Modifier
                .clip(shape = RoundedCornerShape(16.dp))
                .clickable { onClick.invoke() }
                .padding(16.dp)
        else Modifier
                .padding(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            modifier = Modifier.weight(1f, false),
            painter = icon,
            contentDescription = title,
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(5f),
            text = title,
            overflow = Ellipsis,
        )

        Row(
            modifier = Modifier
                .weight(3f)
                .wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content?.invoke()
        }
    }
}

@Composable
fun SettingsItemSwitch(
    title: String,
    icon: Painter,
    item: MutableState<Boolean>
) {

    SettingsItem(
        title = title,
        icon = icon,
        onClick = {
            item.value = !item.value
        }
    ) {
        Switch(
            checked = item.value,
            onCheckedChange = {
                item.value = it
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItemInput(
    title: String,
    icon: Painter,
    item: MutableState<Int>
) {

    val focusManager = LocalFocusManager.current

    SettingsItem(
        title = title,
        icon = icon,
    ) {
        TextField(
            value = item.value.toString(),
            onValueChange = { item.value = it.toIntOrNull() ?: 0 },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                },
            )
        )
    }
}