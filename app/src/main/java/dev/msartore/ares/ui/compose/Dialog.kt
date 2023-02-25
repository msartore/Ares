
package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import dev.msartore.ares.R

@Composable
fun Dialog(
    title: String,
    text: String,
    closeOnClick: Boolean = true,
    onConfirm: () -> Unit,
    onCancel: () -> Unit = {},
    confirmText: String = stringResource(id = R.string.confirm),
    cancelText: String = stringResource(id = R.string.cancel),
    dialogProperties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    ),
    status: MutableState<Boolean> = mutableStateOf(false)
) {
    if (status.value)
        androidx.compose.ui.window.Dialog(
            properties = dialogProperties,
            onDismissRequest = { status.value = false },
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextAuto(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                TextAuto(
                    text = text,
                    maxLines = Int.MAX_VALUE
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onCancel.invoke()
                            if (closeOnClick)
                                status.value = false
                        }
                    ) {
                        TextAuto(text = cancelText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            onConfirm.invoke()
                            if (closeOnClick)
                                status.value = false
                       },
                    ) {
                        TextAuto(text = confirmText)
                    }
                }
            }
        }
}

@Composable
fun DialogContainer(
    dialogProperties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    ),
    status: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    if (status.value)
        androidx.compose.ui.window.Dialog(
            properties = dialogProperties,
            onDismissRequest = { status.value = false },
        ) {
            content()
        }
}
