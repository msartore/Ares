package dev.msartore.ares.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import dev.msartore.ares.R
import dev.msartore.ares.models.FileData

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
                fileType = fileType(displayName)
                icon = when (fileType) {
                    FileType.VIDEO -> R.drawable.video_file_24px
                    FileType.IMAGE -> R.drawable.image_24px
                    FileType.DOCUMENT -> R.drawable.description_24px
                    else -> R.drawable.draft_24px
                }
            }
        }
    }

    return fileData
}

fun fileType(name: String): FileType {
    return when {
        listOf("mp4").any { name.contains(it) } -> FileType.VIDEO
        listOf("jpg", "png").any { name.contains(it) } -> FileType.IMAGE
        listOf("pdf", "txt").any { name.contains(it) } -> FileType.DOCUMENT
        else -> FileType.UNKNOWN
    }
}

enum class FileType {
    VIDEO,
    IMAGE,
    DOCUMENT,
    UNKNOWN
}