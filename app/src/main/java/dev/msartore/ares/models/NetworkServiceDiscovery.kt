package dev.msartore.ares.models

import android.content.ContentValues.TAG
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import dev.msartore.ares.utils.ThreadPerTaskExecutor
import dev.msartore.ares.utils.cor
import dev.msartore.ares.utils.findFreePort
import kotlinx.coroutines.flow.MutableSharedFlow


class NetworkDiscoveryService (
    val servers: MutableSharedFlow<NsdServiceInfo?>
) {

    private var mServiceName = "Ares"
    private var mServiceType = "_ares._tcp"

    private var localPort = 0
    private var nsdManager: NsdManager? = null
    private var isDiscovering = false

    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
            mServiceName = nsdServiceInfo.serviceName
            Log.i(TAG, "onServiceRegistered $nsdServiceInfo")
            discoverServices()
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "onRegistrationFailed code $errorCode\n$serviceInfo")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            Log.i(TAG, "onServiceUnregistered $arg0")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "onRegistrationFailed code $errorCode\n$serviceInfo")
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {

        override fun onDiscoveryStarted(regType: String) {
            Log.i(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            when {
                !service.serviceType.contains(mServiceType) ->
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName == mServiceName ->
                    Log.d(TAG, "Same machine: $mServiceName")
                service.serviceName.contains(mServiceName) -> {
                    if (!isDiscovering) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) nsdManager?.registerServiceInfoCallback(service, ThreadPerTaskExecutor(), serviceInfoCallback)
                        else nsdManager?.resolveService(service, resolveListener)
                    }
                    isDiscovering = true
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {

            if (serviceInfo.serviceName == mServiceName) {
                return
            }

            cor {
                servers.emit(serviceInfo)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val serviceInfoCallback = object : NsdManager.ServiceInfoCallback {
        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {

            if (serviceInfo.serviceName == mServiceName) {
                return
            }

            cor {
                servers.emit(serviceInfo)
            }
        }

        override fun onServiceLost() {
            cor {
                servers.emit(null)
            }
        }

        override fun onServiceInfoCallbackUnregistered() {
            cor {
                servers.emit(null)
            }
        }

    }

    init {
        localPort = findFreePort()
    }

    private fun Context.registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = mServiceName
            serviceType = mServiceType
            port = localPort
        }

        Log.d(TAG, mServiceName + mServiceType + localPort)

        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }

    private fun discoverServices() {
        if (!isDiscovering) {
            nsdManager?.discoverServices(mServiceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }
    }

    fun tearDown() {
        nsdManager?.apply {
            runCatching {
                cor {
                    servers.emit(null)
                }
                unregisterService(registrationListener)
                isDiscovering = false
                stopServiceDiscovery(discoveryListener)
            }
        }
    }

    fun createServices(context: Context) {
        context.registerService()
    }
}

