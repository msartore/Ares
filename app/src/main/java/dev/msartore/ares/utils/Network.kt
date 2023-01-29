package dev.msartore.ares.utils

import android.content.Context
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import dev.msartore.ares.R
import dev.msartore.ares.models.IPSearchData
import dev.msartore.ares.models.NetworkInfo
import dev.msartore.ares.models.Settings
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.server.ServerInfo
import java.net.InetSocketAddress
import java.net.Socket
@ExperimentalGetImage
fun Context.findServers(
    settings: Settings?,
    networkInfo: NetworkInfo,
    ipSearchData: IPSearchData
) {

    if (ipSearchData.isSearching.value == 0) {

        val list = networkInfo.ipAddress.value.split(".")

        ipSearchData.apply {
            ipList.clear()
            isSearching.value = 1
            ipLeft.value = 254

            if (list.size == 4) {
                val firstThree = "${list[0]}.${list[1]}.${list[2]}."
                val last = list.last()

                if (last.isNotEmpty()) {

                    job = work {
                        for (i in 1..254) {

                            if (job.isCancelled)
                                break

                            val ip = firstThree + i

                            ipLeft.value--

                            if (!last.contains(i.toString()))
                                runCatching {
                                    ip.pingServer(settings)

                                    ipSearchData.ipList.add(ServerInfo(ip = firstThree + i))
                                }
                        }

                        isSearching.value--
                    }
                }
            }
        }
    }
    else
        Toast.makeText(this, getString(R.string.wait_still_searching), Toast.LENGTH_SHORT).show()
}

fun isValidServerIP(string: String) =
    string.matches(
        Regex("^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$")
    )

@ExperimentalGetImage
fun String.pingServer(settings: Settings?, timeout: Int? = settings?.ipTimeout?.value) =
    Socket().connect(InetSocketAddress(this, port), timeout ?: 150)