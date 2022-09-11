package dev.msartore.ares.models

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.msartore.ares.MainActivity.MActivity.networkInfo

class NetworkCallback(
    val onNetworkLost: () -> Unit,
    val onNetworkAvailable: (() -> Unit)? = null,
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
            findIPV4(linkProperties) ?: ""
        }.getOrElse {
            networkInfo.isNetworkAvailable.value = false
            ""
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        networkInfo.isNetworkAvailable.value = false
        onNetworkLost()
    }

    private fun findIPV4(linkProperties: LinkProperties):String? {

        linkProperties.linkAddresses.forEach { linkAddress ->
            linkAddress.apply {
                if (address.toString().contains('.'))
                    return address.toString().substring(1)
            }
        }

        return null
    }
}

data class NetworkInfo(
    val isNetworkAvailable: MutableState<Boolean> = mutableStateOf(false),
    val isWifiNetwork: MutableState<Boolean> = mutableStateOf(false),
    val ipAddress: MutableState<String> = mutableStateOf("")
)

