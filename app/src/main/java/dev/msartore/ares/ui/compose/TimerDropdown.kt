package dev.msartore.ares.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TimeDropdown(
    item: MutableState<String>,
    onChange: (String) -> Unit
) {
    var expandedTime by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("05") }
    var selectedUnit by remember { mutableStateOf("mm") }

    val timeOptions = (5 until 60 step(5)).map { it.toString().padStart(2, '0') }
    val unitOptions = listOf("hr", "mm")

    LaunchedEffect(key1 = item.value) {
        item.value.split(':').run {
            if (this.size > 1) {
                selectedTime = this[0]
                selectedUnit = this[1]
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.CenterStart)
            .weight(1f)
        ) {
            TextAuto(
                modifier = Modifier
                    .width(60.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .clickable(onClick = { expandedTime = true }),
                textAlign = TextAlign.Center,
                text = selectedTime
            )

            DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        text = {
                            TextAuto(
                                text = time,
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = {
                        selectedTime = time
                        expandedTime = false
                        onChange("$selectedTime:$selectedUnit")
                    })
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.CenterEnd)
            .weight(1f)
        ) {
            TextAuto(
                modifier = Modifier
                    .width(60.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .clickable(onClick = { expandedUnit = true }),
                textAlign = TextAlign.Center,
                text = selectedUnit
            )

            DropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                unitOptions.forEach { unit ->
                    DropdownMenuItem(
                        text = {
                            TextAuto(
                                text = unit,
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = {
                        selectedUnit = unit
                        expandedUnit = false
                        onChange("$selectedTime:$selectedUnit")
                    })
                }
            }
        }
    }
}
