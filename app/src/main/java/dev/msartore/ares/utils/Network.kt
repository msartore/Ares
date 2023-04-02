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
    settings: Settings?, networkInfo: NetworkInfo, ipSearchData: IPSearchData
) {
    if (!ipSearchData.isSearching.value) {
        ipSearchData.run {
            networkInfo.ipAddress.value?.let { ip ->
                val postFix = ip.substring(ip.lastIndexOf('.') + 1)
                val prefix = ip.substring(0, ip.lastIndexOf('.') + 1)
                ipList.clear()
                isSearching.value = true
                ipLeft.value = 254

                job = work {
                    for (index in 2..254) {
                        if (job?.isCancelled == true) break
                        if (index.toString() == postFix) continue

                        runCatching {
                            settings?.pingServer(prefix + index)

                            ipSearchData.ipList.add(ServerInfo(ip = prefix + index))
                        }

                        ipLeft.value--
                    }

                    isSearching.value = false
                }
            }
        }
    } else Toast.makeText(this, getString(R.string.wait_still_searching), Toast.LENGTH_SHORT).show()
}

fun isValidServerIP(string: String) = string.matches(
    Regex("^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$")
)

@ExperimentalGetImage
fun Settings.pingServer(ip: String, timeout: Int? = ipTimeout.value) =
    Socket().connect(InetSocketAddress(ip, port), timeout ?: 300)