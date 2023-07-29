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
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.camera.core.ExperimentalGetImage
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
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
                    FileType.VIDEO -> R.drawable.movie
                    FileType.IMAGE -> R.drawable.photo
                    FileType.COMPRESSED_ARCHIVE -> R.drawable.file_zip_icon
                    FileType.TEXT -> R.drawable.file_description
                    FileType.CODE -> R.drawable.file_code
                    FileType.BINARY -> R.drawable.file_digit
                    FileType.FONT -> R.drawable.file_typography
                    FileType.RICH_TEXT -> R.drawable.file_text
                    FileType.PDF -> R.drawable.pdf
                    FileType.AUDIO -> R.drawable.file_music
                    FileType.EASEL -> R.drawable.file_vector
                    FileType.APK -> R.drawable.brand_android
                    else -> R.drawable.file_unknown
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
        "7z", "arj", "deb", "pkg", "rar", "rpm", "tar.gz", "z", "zip" -> FileType.COMPRESSED_ARCHIVE
        "apk" -> FileType.APK
        "dxf", "stl", "svg" -> FileType.EASEL
        "fnt", "fon", "otf", "ttf" -> FileType.FONT
        "ppt", "pptx", "key", "odp" -> FileType.PRESENTATION
        "doc", "docx", "odt", "rtf" -> FileType.RICH_TEXT
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
    uuid = this.uuid
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

@ExperimentalGetImage
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

fun checkAvailableSpace(): Long {
    val iStat = StatFs(Environment.getDataDirectory().path)
    val iBlockSize = iStat.blockSizeLong
    val iAvailableBlocks = iStat.availableBlocksLong
    return iAvailableBlocks * iBlockSize
}

fun getCurrentDate(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

fun Context.cleanCache() {
    runCatching {
        work {
            deleteDir(cacheDir)
        }
    }.onFailure {
        it.printStackTrace()
    }
}

fun deleteDir(dir: File?) {
    val files = dir?.listFiles()

    if (files != null) {
        for (file in files) file.delete()
    }
}