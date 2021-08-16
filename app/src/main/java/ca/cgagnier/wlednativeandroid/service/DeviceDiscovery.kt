package ca.cgagnier.wlednativeandroid.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class DeviceDiscovery(context: Context) {

    val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null


    private fun initialize() {
        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)

            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onServiceFound(service: NsdServiceInfo?) {
                Log.d(TAG, "Service discovery success [$service]")
                if (service != null) {
                    when {
                        service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
                            // transport layer for this service.
                            Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                        service.serviceName.contains(SERVICE_NAME) -> nsdManager.resolveService(service, ResolveListener(nsdManager))
                    }
                }

            }

            override fun onServiceLost(service: NsdServiceInfo?) {
                Log.e(TAG, "service lost: $service")
            }
        }
    }

    fun start() {
        stop()
        initialize()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stop() {
        if (discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } finally {
            }

            discoveryListener = null
        }
    }

    class ResolveListener(private val nsdManager: NsdManager): NsdManager.ResolveListener {


        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.e(TAG, "Resolve failed $errorCode")
            when (errorCode) {
                NsdManager.FAILURE_ALREADY_ACTIVE -> {
                    Log.e(TAG, "FAILURE ALREADY ACTIVE")
                    nsdManager.resolveService(serviceInfo, ResolveListener(nsdManager))
                }
                NsdManager.FAILURE_INTERNAL_ERROR -> Log.e(TAG, "FAILURE_INTERNAL_ERROR")
                NsdManager.FAILURE_MAX_LIMIT -> Log.e(TAG, "FAILURE_MAX_LIMIT")
            }
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "Resolve Succeeded. [$serviceInfo]")
        }

    }

    companion object {
        const val TAG = "DEVICE_DISCOVERY"
        const val SERVICE_TYPE = "_wled._tcp."
        const val SERVICE_NAME = "wled"
    }
}