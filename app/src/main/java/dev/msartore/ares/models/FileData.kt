package dev.msartore.ares.models

import android.net.Uri
import androidx.annotation.Keep
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


@Keep
data class FileData(
    val uri: Uri,
    val selected: MutableState<Boolean> = mutableStateOf(false),
    var name: String? = null,
    var size: Int? = null,
    var fileType: FileType? = null,
    var mimeType: String? = null,
    var icon: Int? = null,
)

data class FileDataJson(
    var name: String? = null,
    var size: Int? = null,
    var fileType: FileType? = null,
    var mimeType: String? = null,
    var icon: Int? = null,
    var index: Int? = null
)

enum class FileType {
    VIDEO,
    IMAGE,
    DOCUMENT,
    COMPRESSED_ARCHIVE,
    APK,
    UNKNOWN
}