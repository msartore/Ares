package dev.msartore.ares.utils

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.models.FileType
import dev.msartore.ares.server.KtorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale


fun ContentResolver.extractFileInformation(uri: Uri): FileData? {
    val fileData = FileData(uri = uri)
    val cursor: Cursor? = query(uri, null, null, null, null, null)

    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index < 0) return null

            val displayName: String = it.getString(index)
            val typeIndex = it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
            val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)

            fileData.run {
                size = it.getInt(sizeIndex)
                name = displayName
                fileType = fileType(displayName.lowercase(Locale.ROOT))
                runCatching {
                    mimeType = it.getString(typeIndex)
                }.onFailure {
                    mimeType =
                        getMimeTypeFromExtension(displayName.substring(displayName.lastIndexOf(".") + 1))
                            ?: "application/octet-stream"
                }
                icon = when (fileType) {
                    FileType.VIDEO -> R.drawable.file_earmark_play
                    FileType.IMAGE -> R.drawable.file_earmark_image
                    FileType.COMPRESSED_ARCHIVE -> R.drawable.file_earmark_zip
                    FileType.TEXT -> R.drawable.file_earmark_text
                    FileType.CODE -> R.drawable.file_earmark_code
                    FileType.BINARY -> R.drawable.file_earmark_binary
                    FileType.FONT -> R.drawable.file_earmark_font
                    FileType.RICHTEXT -> R.drawable.file_earmark_richtext
                    FileType.PDF -> R.drawable.file_earmark_pdf
                    FileType.AUDIO -> R.drawable.file_earmark_music
                    FileType.EASEL -> R.drawable.file_earmark_easel
                    else -> R.drawable.file_earmark
                }
            }
        }
    }

    return fileData
}

fun fileType(name: String): FileType {
    val extension = runCatching {
        name.substring(name.lastIndexOf('.') + 1).lowercase()
    }.getOrElse {
        null
    }

    return when (extension) {
        "mp4", "flv", "mkv", "mov", "mpeg", "mpg", "avi", "wmv" -> FileType.VIDEO
        "jpg", "png", "jpeg", "gif", "bmp", "wbmp", "webp" -> FileType.IMAGE
        "cs", "cpp", "c", "java", "js", "py", "rb", "xml" -> FileType.CODE
        "aif", "cda", "mid", "midi", "mp3", "mpa", "ogg", "wav", "wma", "wpl" -> FileType.AUDIO
        "7z", "arj", "deb", "pkg", "rar", "rpm", "tar.gz", "z", "zip", "apk" -> FileType.COMPRESSED_ARCHIVE
        "dxf", "stl", "svg" -> FileType.EASEL
        "fnt", "fon", "otf", "ttf" -> FileType.FONT
        "ppt", "pptx", "key", "odp" -> FileType.PRESENTATION
        "doc", "docx", "odt", "rtf" -> FileType.RICHTEXT
        "bin", "dat", "exe", "out" -> FileType.BINARY
        "text" -> FileType.TEXT
        "pdf" -> FileType.PDF
        else -> FileType.UNKNOWN
    }
}

fun splitFileTypeFromName(text: String): Pair<String, String> {
    return Pair(
        text.substring(0, text.indexOfLast { it == '.' }),
        text.substring(text.indexOfLast { it == '.' }, text.length)
    )
}

fun String.contains(collection: Collection<String>) = collection.any { this.contains(it) }

fun Int.printableSize() = when (this) {
    in 0..999 -> "$this B"
    in 1000..999999 -> "%.2f".format(this / 1000.0) + " KB"
    in 1000000..1000000000 -> "%.2f".format(this / 1000000.0) + " MB"
    else -> "%.2f".format(this / 1000000000.0) + " GB"
}

fun FileData.toFileDataJson() = FileDataJson(
    name = this.name,
    size = this.size,
    text = this.text,
    fileType = this.fileType,
    mimeType = this.mimeType,
    icon = this.icon,
    UUID = this.UUID
)

fun Any.toJson(): String = Gson().toJson(this)

fun Collection<FileDataJson>.toJsonArray(): JsonArray {
    val array = JsonArray()

    this.forEach {
        array.add(JsonPrimitive(it.toJson()))
    }

    return array
}

fun getByteArrayFromDrawable(context: Context, id: Int, color: Int? = null) =
    getDrawable(context, id)?.let {
        if (color != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    color, BlendModeCompat.SRC_ATOP
                )
            } else {
                @Suppress("DEPRECATION") it.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        }
        val bitmap = it.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }

suspend fun Context.filesDataHandler(isLoading: MutableStateFlow<Boolean>, uris: List<Uri>?) {
    val listFileSizeB = KtorService.KtorServer.concurrentMutableList.size.value

    if (!uris.isNullOrEmpty()) {
        isLoading.value = true

        KtorService.KtorServer.concurrentMutableList.run {
            addAll(uris.filter { uri ->
                this.list.none { it.uri == uri }
            }.mapNotNull {
                runCatching { contentResolver.extractFileInformation(it) }.getOrNull()
            })
        }

        if (listFileSizeB == KtorService.KtorServer.concurrentMutableList.size.value) cor {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext, getString(R.string.removed_duplicates), Toast.LENGTH_SHORT
                ).show()
            }
        }

        isLoading.value = false
    }
}

fun getMimeTypeFromExtension(extension: String): String? {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}

@Suppress("DEPRECATION")
fun Context.packageInfo(): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
            packageName, PackageManager.PackageInfoFlags.of(0L)
        )
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }