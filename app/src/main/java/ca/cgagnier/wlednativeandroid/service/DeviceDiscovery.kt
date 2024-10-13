package ca.cgagnier.wlednativeandroid.service

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ca.cgagnier.wlednativeandroid.model.Device
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder


class DeviceDiscovery(
    val context: Context,
    val onDeviceDiscovered: (Device) -> Unit
) {

    val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val multicastLock: MulticastLock = wifi.createMulticastLock("multicastLock")

    private val parent = this

    private fun initialize() {
        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: Error code:$errorCode")
                try {
                    nsdManager.stopServiceDiscovery(this)
                } finally {
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: Error code:$errorCode")
                try {
                    nsdManager.stopServiceDiscovery(this)
                } finally {
                }
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(TAG, "Service discovery started: $serviceType")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onServiceFound(service: NsdServiceInfo?) {
                Log.d(TAG, "Service discovery success [$service]")
                if (service != null) {
                    if (service.serviceType != SERVICE_TYPE) {
                        Log.d(TAG, "Unknown service type: ${service.serviceType}")
                        return
                    }
                    return nsdManager.resolveService(
                        service,
                        ResolveListener(nsdManager) { onServiceResolved(it) }
                    )
                }
            }

            override fun onServiceLost(service: NsdServiceInfo?) {
                Log.e(TAG, "service lost: $service")
            }
        }
    }

    private fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Log.i(TAG, "Device discovered!")

        val deviceIp = serviceInfo.host.hostAddress!!
        val deviceName = serviceInfo.serviceName ?: ""
        onDeviceDiscovered(
            Device(
                deviceIp,
                deviceName,
                isCustomName = false,
                isHidden = false,
                macAddress = Device.UNKNOWN_VALUE
            )
        )
    }

    fun start() {
        stop()

        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()

        initialize()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stop() {
        if (multicastLock.isHeld) {
            multicastLock.release()
        }
        if (discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop: ${e.message}", e)
            }

            discoveryListener = null
        }
    }

    class ResolveListener(
        private val nsdManager: NsdManager,
        private val serviceResolved: (NsdServiceInfo) -> Unit
    ): NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.e(TAG, "Resolve failed $errorCode")
            when (errorCode) {
                NsdManager.FAILURE_ALREADY_ACTIVE -> {
                    Log.e(TAG, "FAILURE ALREADY ACTIVE")
                    nsdManager.resolveService(
                        serviceInfo, ResolveListener(nsdManager, serviceResolved)
                    )
                }

                NsdManager.FAILURE_INTERNAL_ERROR -> {
                    Log.e(TAG, "FAILURE_INTERNAL_ERROR")
                }

                NsdManager.FAILURE_MAX_LIMIT -> {
                    Log.e(TAG, "FAILURE_MAX_LIMIT")
                }

                else -> Log.e(TAG, "Resolve failed")
            }
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "Resolve Succeeded. [$serviceInfo]")
            if (serviceInfo != null) {
                serviceResolved(serviceInfo)
            } else {
                Log.e(TAG, "Resolve Succeeded, but serviceInfo null.")
            }
        }

    }

    companion object {
        private const val TAG = "DEVICE_DISCOVERY"
        const val SERVICE_TYPE = "_wled._tcp."

        private const val DEFAULT_WLED_AP_IP = "4.3.2.1"


        @SuppressLint("WifiManagerPotentialLeak")
        fun isConnectedToWledAP(applicationContext: Context): Boolean {
            val manager = applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
            val dhcp = manager.dhcpInfo
            var ip = dhcp.gateway
            ip =
                if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) Integer.reverseBytes(ip)
                else ip

            val ipAddressByte: ByteArray = BigInteger.valueOf(ip.toLong()).toByteArray()
            if (ipAddressByte.isEmpty() || ipAddressByte.size <= 1) {
                Log.w(TAG, "IP Address is empty or smaller than 1")
                return false
            }

            val inetAddress: InetAddress = InetAddress.getByAddress(ipAddressByte)
            val ipAddress = inetAddress.hostAddress

            Log.w(TAG, "Ip address: $ipAddress")
            return ipAddress == DEFAULT_WLED_AP_IP
        }

        fun getDefaultAPDevice(): Device {
            return Device(
                address = DEFAULT_WLED_AP_IP,
                name = "WLED AP Mode",
                isCustomName = true,
                isHidden = true,
                macAddress = Device.UNKNOWN_VALUE
            )
        }
    }
}