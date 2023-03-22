package dev.msartore.ares.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import dev.msartore.ares.R

fun DownloadManager.downloadFile(
    url: String, fileName: String, mimeType: String?, context: Context
): Long {
    val request = DownloadManager.Request(Uri.parse(url))

    request.run {
        setAllowedOverRoaming(false)
        setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        setTitle(fileName)
        setMimeType(mimeType)
        setDescription(context.getString(R.string.download_description) + fileName)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
    }

    return enqueue(request)
}