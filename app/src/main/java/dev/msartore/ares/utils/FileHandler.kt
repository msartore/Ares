package dev.msartore.ares.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.models.FileType
import java.io.ByteArrayOutputStream
import java.util.Locale


fun ContentResolver.extractFileInformation(uri: Uri): FileData? {

    val fileData = FileData(uri = uri)
    val cursor: Cursor? = query(uri, null, null, null, null, null)

    cursor?.use {

        if (it.moveToFirst()) {

            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index < 0)
                return null

            val displayName: String = it.getString(index)
            val typeIndex =  it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
            val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)

            fileData.apply {
                size = it.getInt(sizeIndex)
                name = displayName
                fileType = fileType(displayName.lowercase(Locale.ROOT))
                mimeType = it.getString(typeIndex)
                icon = when (fileType) {
                    FileType.VIDEO -> R.drawable.video_file_24px
                    FileType.IMAGE -> R.drawable.image_24px
                    FileType.DOCUMENT -> R.drawable.description_24px
                    FileType.COMPRESSED_ARCHIVE -> R.drawable.folder_zip_24px
                    FileType.APK -> R.drawable.apk_document_24px
                    else -> R.drawable.draft_24px
                }
            }
        }
    }

    return fileData
}

fun fileType(name: String): FileType {
    return when {
        name.contains(listOf("mp4", "wav", "mpg", "mpeg", "mp4", "3gp", "3gpp", "mkv", "avi")) -> FileType.VIDEO
        name.contains(listOf("jpg", "png", "jpeg", "gif", "bmp", "wbmp", "webp")) -> FileType.IMAGE
        name.contains(listOf("pdf", "txt", "html", "htm")) -> FileType.DOCUMENT
        name.contains(listOf("apk")) -> FileType.APK
        name.contains(listOf("zip", "rar", "tar")) -> FileType.COMPRESSED_ARCHIVE
        else -> FileType.UNKNOWN
    }
}

fun splitFileTypeFromName(text: String): Pair<String, String> {
    return Pair(text.substring(0, text.indexOfLast { it == '.' }), text.substring(text.indexOfLast { it == '.' }, text.length))
}

fun String.contains(collection: Collection<String>) =
    collection.any { this.contains(it) }

fun Int.printableSize() =
    when(this) {
        in 0..10000 -> "${this/1}B"
        in 10000..1000000 -> "%.2f".format(this/1000.0) + "KB"
        in 1000000..1000000000 -> "%.2f".format(this/1000000.0) + "MB"
        else -> "%.2f".format(this/1000000000.0) + "GB"
    }

fun FileData.toFileDataJson(index: Int?) =
    FileDataJson(
        name = this.name,
        size = this.size,
        fileType = this.fileType,
        mimeType = mimeType,
        icon = this.icon,
        index = index
    )

fun FileDataJson.toJson(): String =
    Gson().toJson(this)

fun Collection<FileDataJson>.toJsonArray(): JsonArray {
    val array = JsonArray()

    this.forEach {
        array.add(JsonPrimitive(it.toJson()))
    }

    return array
}

fun getByteArrayFromDrawable(context: Context, id: Int) =
    getDrawable(context, id)?.let {
        val bitmap = it.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }