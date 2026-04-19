package dev.msartore.ares.models

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.utils.cor
import dev.msartore.ares.utils.encodeAsBitmap


class NetworkCallback(
    val onNetworkLost: () -> Unit,
    private val onNetworkAvailable: (() -> Unit)? = null,
    val networkInfo: NetworkInfo
) : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        networkInfo.isNetworkAvailable.value = true
        onNetworkAvailable?.invoke()
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        networkInfo.isWifiNetwork.value = networkCapabilities.hasTransport(TRANSPORT_WIFI)
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)

        networkInfo.ipAddress.value = runCatching {
            val ip = findIPV4(linkProperties)

            cor {
                networkInfo.bitmap.value =
                    encodeAsBitmap("http://$ip:$port", 500, 500).asImageBitmap()
            }

            ip
        }.getOrElse {
            null
        }

        networkInfo.isNetworkAvailable.value = !networkInfo.ipAddress.value.isNullOrEmpty()
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        if (!networkInfo.isWifiNetwork.value) {
            Log.e("Connection", "Connection lost")
            networkInfo.isNetworkAvailable.value = false
            onNetworkLost()
        }
    }

    private fun findIPV4(linkProperties: LinkProperties): String? {
        var fallback: String? = null
        linkProperties.linkAddresses.forEach { linkAddress ->
            val raw = linkAddress.address.hostAddress ?: return@forEach
            if (!raw.contains('.')) return@forEach  // skip IPv6
            if (raw.startsWith("169.254.")) return@forEach  // skip link-local
            if (raw.startsWith("192.168.") || raw.startsWith("10.") ||
                raw.matches(Regex("^172\\.(1[6-9]|2[0-9]|3[01])\\..+"))
            ) return raw
            if (fallback == null) fallback = raw
        }
        return fallback
    }
}

data class NetworkInfo(
    val isNetworkAvailable: MutableState<Boolean> = mutableStateOf(false),
    val isWifiNetwork: MutableState<Boolean> = mutableStateOf(false),
    val ipAddress: MutableState<String?> = mutableStateOf(null),
    var bitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
)

