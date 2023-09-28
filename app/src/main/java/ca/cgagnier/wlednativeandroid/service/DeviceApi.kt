package ca.cgagnier.wlednativeandroid.service

import android.graphics.Color
import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.service.api.JsonApi
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object DeviceApi {
    private const val TAG = "DeviceApi"
    private var application: DevicesApplication? = null
    @OptIn(DelicateCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext(TAG))

    fun setApplication(devicesApplication: DevicesApplication) {
        application = devicesApplication
    }

    fun update(device: Device, silentUpdate: Boolean, callback: ((Device) -> Unit)? = null) {
        if (!silentUpdate) {
            val newDevice = device.copy(isRefreshing = true)

            scope.launch {
                application!!.deviceRepository.update(newDevice)
            }
        }

        val stateInfoCall: Call<DeviceStateInfo>
        try {
            stateInfoCall = getJsonApi(device).getStateInfo()
        } catch (e: IllegalArgumentException) {
            Log.wtf(TAG, "Device has invalid address: " + device.address)
            Firebase.crashlytics.recordException(e)
            scope.launch {
                application!!.deviceRepository.delete(device)
            }
            return
        }

        stateInfoCall.enqueue(object : Callback<DeviceStateInfo> {
            override fun onResponse(call: Call<DeviceStateInfo>, response: Response<DeviceStateInfo>) =
                onSuccess(device, response, callback)

            override fun onFailure(call: Call<DeviceStateInfo>, t: Throwable) =
                onFailure(device, t, callback)
        })
    }

    fun postJson(device: Device, jsonData: JsonPost) {
        Log.d(TAG, "Posting update to device [${device.address}]")

        val stateInfoCall = getJsonApi(device).postJson(jsonData)
        stateInfoCall.enqueue(object : Callback<DeviceStateInfo> {
            override fun onResponse(call: Call<DeviceStateInfo>, response: Response<DeviceStateInfo>) =
                onSuccess(device, response)

            override fun onFailure(call: Call<DeviceStateInfo>, t: Throwable) =
                onFailure(device, t)
        })
    }

    private fun getJsonApi(device: Device): JsonApi {
        return Retrofit.Builder()
            .baseUrl(device.getDeviceUrl())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(JsonApi::class.java)
    }

    private fun onFailure(device: Device, t: Throwable? = null, callback: ((Device) -> Unit)? = null) {
        if (t != null) {
            Log.e(TAG, t.message!!)
            Firebase.crashlytics.recordException(t)
        }
        val updatedDevice = device.copy(isOnline = false, isRefreshing = false)
        if (callback != null) {
            callback(updatedDevice)
            return
        }
        scope.launch {
            application!!.deviceRepository.update(updatedDevice)
        }
    }

    fun onSuccess(device: Device, response: Response<DeviceStateInfo>, callback: ((Device) -> Unit)? = null) {
        if (response.code() == 200 && response.isSuccessful && response.body() != null) {
            val deviceStateInfo = response.body()!!
            val colorInfo = deviceStateInfo.state.segment?.get(0)?.colors?.get(0)

            val updatedDevice = device.copy(
                macAddress = deviceStateInfo.info.mac ?: Device.UNKNOWN_VALUE,
                isOnline = true,
                name = if (device.isCustomName) device.name else deviceStateInfo.info.name,
                brightness = if (device.isSliding) device.brightness else deviceStateInfo.state.brightness,
                isPoweredOn = deviceStateInfo.state.isOn,
                color = if (colorInfo != null) Color.rgb(
                    colorInfo[0],
                    colorInfo[1],
                    colorInfo[2]
                ) else Color.WHITE,
                isRefreshing = false,
                networkRssi = deviceStateInfo.info.wifi.rssi ?: 0,
                isEthernet = false,
                platformName = deviceStateInfo.info.platformName ?: Device.UNKNOWN_VALUE,
                version = deviceStateInfo.info.version ?: Device.UNKNOWN_VALUE
            )

            if (callback != null) {
                callback(updatedDevice)
                return
            }
            if (updatedDevice != device) {
                scope.launch {
                    application!!.deviceRepository.update(updatedDevice)
                }
            }
        } else {
            Firebase.crashlytics.log("Response success, but not valid")
            Firebase.crashlytics.setCustomKey("response code", response.code())
            Firebase.crashlytics.setCustomKey("response isSuccessful", response.isSuccessful)
            Firebase.crashlytics.setCustomKey("response errorBody", response.errorBody().toString())
            Firebase.crashlytics.setCustomKey("response headers", response.headers().toString())

            onFailure(device, Exception("Response success, but not valid"), callback = callback)
        }
    }
}
