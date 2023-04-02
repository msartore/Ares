package dev.msartore.ares.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    icon: Painter,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val modifier = if (onClick != null) Modifier
        .clip(shape = RoundedCornerShape(16.dp))
        .clickable { onClick.invoke() }
        .padding(16.dp)
    else Modifier.padding(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(5f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
            )

            Column {
                TextAuto(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = Int.MAX_VALUE,
                    lineHeight = 22.sp
                )

                if (description != null) TextAuto(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = Int.MAX_VALUE,
                )
            }
        }

        if (content != null) {
            Row(
                modifier = Modifier
                    .weight(3f)
                    .wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content.invoke()
            }
        }
    }
}

@Composable
fun SettingsItemSwitch(
    title: String,
    description: String? = null,
    icon: Painter,
    item: MutableState<Boolean>,
    onClick: (() -> Unit)? = null
) {
    SettingsItem(title = title, description = description, icon = icon, onClick = {
        item.value = !item.value
        onClick?.invoke()
    }) {
        Switch(checked = item.value, onCheckedChange = {
            item.value = it
            onClick?.invoke()
        })
    }
}

@Composable
fun SettingsItemInput(
    title: String,
    description: String? = null,
    icon: Painter,
    item: MutableState<Int>,
    onCheck: ((String) -> Boolean)? = null,
    onClick: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val itemUI = remember { mutableStateOf(item.value.toString()) }
    val isFocused = remember { mutableStateOf(false) }

    BackHandler(isFocused.value) {
        itemUI.value = item.value.toString()
        focusManager.clearFocus()
    }

    SettingsItem(
        title = title,
        description = description,
        icon = icon,
    ) {
        TextField(modifier = Modifier.onFocusEvent { focusState ->
                isFocused.value = focusState.isFocused
            }, value = itemUI.value, onValueChange = {
            itemUI.value = it
        }, keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
        ), keyboardActions = KeyboardActions(
            onDone = {
                if (onCheck?.invoke(itemUI.value) != false) {
                    item.value = itemUI.value.toIntOrNull() ?: 0
                    itemUI.value = item.value.toString()
                    onClick?.invoke()
                    focusManager.clearFocus()
                } else {
                    itemUI.value = item.value.toString()
                    onClick?.invoke()
                    focusManager.clearFocus()
                }
            },
        )
        )
    }
}