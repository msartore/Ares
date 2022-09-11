package dev.msartore.ares.models

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.msartore.ares.utils.FileType

data class FileData(
    val uri: Uri,
    val selected: MutableState<Boolean> = mutableStateOf(false),
    var name: String? = null,
    var size: Int? = null,
    var fileType: FileType? = null,
    var icon: Int? = null,
)