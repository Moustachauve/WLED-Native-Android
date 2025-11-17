package ca.cgagnier.wlednativeandroid.service.websocket

import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow

class WebsocketClient(device: Device) {

    val deviceState: DeviceWithState = DeviceWithState(device)

    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .build()
    private var isConnecting = false
    private var retryCount = 0

    // Moshi setup
    private val moshi: Moshi = Moshi.Builder().build()
    private val deviceStateInfoJsonAdapter: JsonAdapter<DeviceStateInfo> =
        moshi.adapter(DeviceStateInfo::class.java)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "WebsocketClient"
        private const val RECONNECTION_DELAY = 2500L // 2.5 seconds
        private const val MAX_RECONNECTION_DELAY = 120000L // 120 seconds
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected for ${deviceState.device.address}")
            deviceState.isWebsocketConnected.value = true
            retryCount = 0
            isConnecting = false
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "onMessage for ${deviceState.device.address}: $text")
            try {
                // Use the pre-created Moshi adapter
                val deviceStateInfo = deviceStateInfoJsonAdapter.fromJson(text)
                if (deviceStateInfo != null) {
                    deviceState.stateInfo.value = deviceStateInfo

                    // This should probably be done in a ViewModel or a repository
                    // deviceState.device.originalName = deviceStateInfo.info.name
                    // db.devices.update(this.device.macAddress, this.state.device);
                } else {
                    Log.w(TAG, "Received a null message after parsing.")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to parse JSON from WebSocket", e)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(
                TAG,
                "WebSocket closing for ${deviceState.device.address}. Code: $code, Reason: $reason"
            )
            deviceState.isWebsocketConnected.value = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(
                TAG,
                "WebSocket failure for ${deviceState.device.address}: ${t.message}",
                t
            )
            deviceState.isWebsocketConnected.value = false
            isConnecting = false
            reconnect()
        }
    }

    fun connect() {
        if (webSocket != null || isConnecting) {
            Log.w(TAG, "Already connected or connecting to ${deviceState.device.address}")
            return
        }
        isConnecting = true
        val websocketUrl = "ws://${deviceState.device.address}/ws"
        val request = Request.Builder().url(websocketUrl).build()
        Log.d(TAG, "Connecting to ${deviceState.device.address}")
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    private fun reconnect() {
        if (isConnecting) return

        coroutineScope.launch {
            val delay = min(
                RECONNECTION_DELAY * 2.0.pow(retryCount).toLong(),
                MAX_RECONNECTION_DELAY
            )
            Log.d(TAG, "Reconnecting to ${deviceState.device.address} in ${delay / 1000}s")
            delay(delay)
            retryCount++
            connect()
        }
    }

    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun destroy() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, "Client destroyed")
        webSocket = null
    }
}