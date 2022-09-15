package dev.msartore.ares.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData
import java.util.*

fun ContentResolver.extractFileInformation(uri: Uri): FileData? {

    val fileData = FileData(uri = uri)
    val cursor: Cursor? = query(uri, null, null, null, null, null)

    cursor?.use {

        if (it.moveToFirst()) {

            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index < 0)
                return null

            val displayName: String = it.getString(index)

            val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
            val displaySize = it.getInt(sizeIndex)

            fileData.apply {
                size = displaySize
                name = displayName
                fileType = fileType(displayName.lowercase(Locale.ROOT))
                icon = when (fileType) {
                    FileType.VIDEO -> R.drawable.video_file_24px
                    FileType.IMAGE -> R.drawable.image_24px
                    FileType.DOCUMENT -> R.drawable.description_24px
                    FileType.ZIP -> R.drawable.folder_zip_24px
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
        name.contains(listOf("mp4", "mkv", "webm", "3gp")) -> FileType.VIDEO
        name.contains(listOf("jpg", "png", "raw", "psd", "bmp", "jpeg", "gif", "dng", "tiff")) -> FileType.IMAGE
        name.contains(listOf("pdf", "txt", "docs")) -> FileType.DOCUMENT
        name.contains(listOf("apk")) -> FileType.APK
        name.contains(listOf("zip")) -> FileType.ZIP
        else -> FileType.UNKNOWN
    }
}

fun String.contains(collection: Collection<String>) =
    collection.any { this.contains(it) }

enum class FileType {
    VIDEO,
    IMAGE,
    DOCUMENT,
    ZIP,
    APK,
    UNKNOWN
}