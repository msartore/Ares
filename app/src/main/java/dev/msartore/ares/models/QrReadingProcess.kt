package dev.msartore.ares.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class QrReadingProcess(
    val isReadingQR: MutableState<Boolean> = mutableStateOf(false),
    val errorStatusDialog: MutableState<Boolean> = mutableStateOf(false),
    val isPingingServer: MutableState<Boolean> = mutableStateOf(false)
)