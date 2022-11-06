package ca.cgagnier.wlednativeandroid.service

import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import kotlinx.coroutines.*

object ThrottleApiPostCall {
    private const val TAG = "ThrottleApiPostCall"
    private const val TIME_MS_BETWEEN_CALL: Long = 250

    private var alreadySent = true
    private var sendPending = false
    private var nextTargetDevice: Device? = null
    private var nextJsonData: JsonPost? = null

    private var sendJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun send(device: Device, jsonData: JsonPost) {
        Log.d(TAG, "Queueing [${jsonData}]")

        nextTargetDevice = device
        nextJsonData = jsonData
        alreadySent = false

        if (sendPending) {
            return
        }

        sendPending = true

        sendJob = GlobalScope.launch {
            delay(TIME_MS_BETWEEN_CALL)
            if (!alreadySent) {
                Log.d(TAG, "Posting from coroutine [${nextJsonData}]")
                DeviceApi.postJson(nextTargetDevice!!, nextJsonData!!)
                alreadySent = true
                sendPending = false
            }
        }
    }
}