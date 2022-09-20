package dev.msartore.ares.utils

import com.google.gson.Gson
import dev.msartore.ares.MainActivity.MActivity.client
import dev.msartore.ares.models.FileDataJson
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import io.ktor.client.call.*
import io.ktor.client.request.*

suspend fun serverInfoExtraction(ip: String): List<FileDataJson>? {

    val response = client.get("http://$ip:${PORT}/info")

    return if (response.status.value in 200..299) {
        Gson().fromJson(response.body<String>(), mutableListOf<String>().javaClass).map {
            Gson().fromJson(it, FileDataJson::class.java)
        }
    }
    else null
}