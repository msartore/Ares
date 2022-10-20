package dev.msartore.ares.models

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.utils.cor
import dev.msartore.ares.utils.encodeAsBitmap
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

@ExperimentalGetImage
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
            val ip = findIPV4(linkProperties) ?: ""

            cor {
                networkInfo.bitmap.value = encodeAsBitmap("http://$ip:$PORT", 500, 500)?.asImageBitmap()
            }

            ip
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
    val ipAddress: MutableState<String> = mutableStateOf(""),
    var bitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
)

data class FileTransfer(
    var pipelineContext: PipelineContext<Unit, ApplicationCall>? = null,
    val isActive: MutableState<Boolean> = mutableStateOf(false),
    var sizeTransferred: MutableState<Float> = mutableStateOf(0f),
    var size: Int? = null,
    var name: String? = null,
)
