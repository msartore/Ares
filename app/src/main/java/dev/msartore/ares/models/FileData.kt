package dev.msartore.ares.models

import android.net.Uri
import android.os.CountDownTimer
import androidx.annotation.Keep
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

@Keep
data class FileData(
    val uri: Uri? = null,
    var text: String? = null,
    val selected: MutableState<Boolean> = mutableStateOf(false),
    val UUID: UUID = java.util.UUID.randomUUID(),
    var name: String? = null,
    var size: Int? = null,
    var fileType: FileType? = null,
    var mimeType: String? = null,
    var icon: Int? = null,
)

@Keep
data class FileDataJson(
    var name: String? = null,
    var text: String? = null,
    val UUID: UUID? = null,
    var size: Int? = null,
    var fileType: FileType? = null,
    var mimeType: String? = null,
    var icon: Int? = null
)

@Keep
enum class FileType {
    VIDEO, IMAGE, DOCUMENT, TEXT, COMPRESSED_ARCHIVE, APK, UNKNOWN
}

data class FileDownload(
    var fileData: FileData? = null,
    val state: MutableState<Boolean> = mutableStateOf(false),
    val timerScheduler: CountDownTimer? = object : CountDownTimer(3600, 3600) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            state.value = false
        }
    }
)