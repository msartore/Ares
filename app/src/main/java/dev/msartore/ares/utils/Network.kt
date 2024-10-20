package dev.msartore.ares.utils

import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.camera.core.ExperimentalGetImage
import dev.msartore.ares.models.Settings
import dev.msartore.ares.server.KtorService.KtorServer.port
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

fun isValidServerIP(string: String) = string.matches(
    Regex("^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$")
)

@ExperimentalGetImage
fun Settings.pingServer(ip: String, timeout: Int? = ipTimeout.value) =
    Socket().connect(InetSocketAddress(ip, port), timeout ?: 300)

fun findFreePort(): Int {
    val socket = ServerSocket(0)
    socket.reuseAddress = true

    val port = socket.localPort
    socket.close()

    return port
}

fun getIpAndPort(nsdServiceInfo: NsdServiceInfo): Pair<String?, Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        nsdServiceInfo.hostAddresses[0].run {
            Pair(hostAddress, nsdServiceInfo.port)
        }
    }
    else {
        nsdServiceInfo.host.run {
            Pair(hostAddress, nsdServiceInfo.port)
        }
    }
}