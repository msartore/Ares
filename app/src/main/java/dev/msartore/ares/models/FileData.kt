package dev.msartore.ares.models

import android.net.Uri
import android.os.CountDownTimer
import androidx.annotation.Keep
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.io.File
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
    VIDEO, IMAGE, TEXT, COMPRESSED_ARCHIVE, CODE, BINARY, FONT, RICHTEXT, PDF, AUDIO, EASEL, PRESENTATION, UNKNOWN,
}

data class FileDownload(
    var fileData: FileData? = null,
    var onFinish: (() -> Unit)? = null,
    val timerScheduler: CountDownTimer? = object : CountDownTimer(7200, 3600) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            onFinish?.invoke()
        }
    }
)

data class FileTransfer(
    var pipelineContext: PipelineContext<Unit, ApplicationCall>? = null,
    val isActive: MutableState<Boolean> = mutableStateOf(false),
    var sizeTransferred: MutableState<Float> = mutableStateOf(0f),
    var size: Int? = null,
    var name: String? = null,
    var onFileTransferred: ((File) -> Unit)? = null
)
