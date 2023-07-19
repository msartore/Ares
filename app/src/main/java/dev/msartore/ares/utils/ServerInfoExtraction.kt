package dev.msartore.ares.utils

import androidx.camera.core.ExperimentalGetImage
import com.google.gson.Gson
import dev.msartore.ares.models.APIData
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.server.KtorService.KtorServer.port
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*

@ExperimentalGetImage
suspend fun serverInfoExtraction(
    ip: String, client: HttpClient
): Pair<String, List<FileDataJson>>? {
    val response = client.get("http://$ip:$port/info")

    return if (response.status.value in 200..299) {
        Gson().fromJson(response.body<String>(), APIData::class.java).run {
            Pair(
                appVersion,
                Gson().fromJson(collection, mutableListOf<String>().javaClass).map {
                    Gson().fromJson(it, FileDataJson::class.java)
                }
            )
        }
    } else null
}

fun timeToMillis(timeString: String): Long {

    if (timeString.isEmpty()) return 0

    val parts = timeString.split(":")
    val time = parts[0].toInt()

    return when (val unit = parts[1]) {
        "hr" -> time * 60 * 60 * 1000L
        "mm" -> time * 60 * 1000L
        else -> throw IllegalArgumentException("Invalid time unit: $unit")
    }
}
