package ca.cgagnier.wlednativeandroid.service

import android.graphics.Color
import android.util.Log
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.model.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.JsonPost
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.api.JsonApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object DeviceApi {
    private const val TAG = "DeviceApi"

    private fun getJsonApi(device: DeviceItem): JsonApi {
        return Retrofit.Builder()
            .baseUrl(device.getDeviceUrl())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(JsonApi::class.java)
    }

    fun update(device: DeviceItem) {

        val newDevice = device.copy(isRefreshing = true)
        DeviceRepository.put(newDevice)

        val stateInfoCall = getJsonApi(device).getStateInfo()
        stateInfoCall.enqueue(object : Callback<DeviceStateInfo> {
            override fun onResponse(call: Call<DeviceStateInfo>, response: Response<DeviceStateInfo>) =
                onSuccess(device, response)

            override fun onFailure(call: Call<DeviceStateInfo>, t: Throwable) =
                onFailure(device, t)
        })
    }

    fun postJson(device: DeviceItem, jsonData: JsonPost) {
        Log.d(TAG, "Posting update to device [${device.address}]")

        val stateInfoCall = getJsonApi(device).postJson(jsonData)
        stateInfoCall.enqueue(object : Callback<DeviceStateInfo> {
            override fun onResponse(call: Call<DeviceStateInfo>, response: Response<DeviceStateInfo>) =
                onSuccess(device, response)

            override fun onFailure(call: Call<DeviceStateInfo>, t: Throwable) =
                onFailure(device, t)
        })
    }

    private fun onFailure(device: DeviceItem, t: Throwable? = null) {
        if (t != null) {
            Log.e(TAG, t.message!!)
        }
        val updatedDevice = device.copy(isOnline = false, isRefreshing = false)
        DeviceRepository.put(updatedDevice)
    }

    fun onSuccess(device: DeviceItem, response: Response<DeviceStateInfo>) {
        if (response.code() == 200) {
            val deviceStateInfo = response.body()!!
            val colorInfo = deviceStateInfo.state.segment[0].colors[0]

            val updatedDevice = device.copy(
                isOnline = true,
                name = if (device.isCustomName) device.name else deviceStateInfo.info.name,
                brightness = if (device.isSliding) device.brightness else deviceStateInfo.state.brightness,
                isPoweredOn = deviceStateInfo.state.isOn,
                color = Color.rgb(colorInfo[0], colorInfo[1], colorInfo[2]),
                isRefreshing = false,
                networkRssi = deviceStateInfo.info.wifi.rssi
            )

            DeviceRepository.put(updatedDevice)
        } else {
            onFailure(device)
        }
    }
}
