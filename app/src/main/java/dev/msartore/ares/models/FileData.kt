package dev.msartore.ares.models

import android.net.Uri
import android.os.CountDownTimer
import androidx.annotation.Keep
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.JsonArray
import dev.msartore.ares.utils.cor
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.util.UUID

@Keep
data class APIData(
    val collection: JsonArray,
    val appVersion: String
)

@Keep
data class FileData(
    val uri: Uri? = null,
    var text: String? = null,
    val selected: MutableState<Boolean> = mutableStateOf(false),
    val uuid: UUID = UUID.randomUUID(),
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
    val uuid: UUID? = null,
    var size: Int? = null,
    var fileType: FileType? = null,
    var mimeType: String? = null,
    var icon: Int? = null
)

@Keep
enum class FileType {
    VIDEO, IMAGE, TEXT, COMPRESSED_ARCHIVE, CODE, BINARY, FONT, RICH_TEXT, PDF, AUDIO, EASEL, PRESENTATION, APK, UNKNOWN,
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

class FileTransfer(
    var pipelineContext: PipelineContext<Unit, ApplicationCall>? = null,
    val status: MutableState<FileTransferStages> = mutableStateOf(FileTransferStages.INACTIVE),
    var sizeTransferred: MutableState<Float> = mutableFloatStateOf(0f),
    var size: Int? = null,
    var name: MutableState<String> = mutableStateOf(""),
    var onFileTransferred: ((File) -> Unit)? = null,
    var file: File? = null,
    var cancelled: Boolean = false,
) {
    fun reset() {
        cor {
            status.value = FileTransferStages.INACTIVE
            pipelineContext = null
            size = null
            file = null
            sizeTransferred.value = 0f
            name.value = ""
            cancelled = false
        }
    }
}

data class FileZip(
    var file: File? = null,
    var version: Int = -1
)

enum class FileTransferStages {
    INITIALIZING,
    ARCHIVING,
    TRANSMITTING,
    FINALIZING,
    INACTIVE
}

data class TransferFile(
    val fileData: FileData,
    val transferStages: TransferFileType,
    val viewed: MutableState<Boolean> = mutableStateOf(false)
)

enum class TransferFileType {
    UPLOAD,
    DOWNLOAD
}