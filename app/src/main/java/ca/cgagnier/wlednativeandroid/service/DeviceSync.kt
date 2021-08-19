package ca.cgagnier.wlednativeandroid.service

import android.util.Log
import ca.cgagnier.wlednativeandroid.DeviceItem
import ca.cgagnier.wlednativeandroid.model.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.api.StateInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object DeviceSync {
    private const val TAG = "DeviceSync"

    fun update(device: DeviceItem) {
        val retrofit = Retrofit.Builder()
            .baseUrl(device.getDeviceUrl())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val stateInfoEndpoint = retrofit.create(StateInfo::class.java)
        val stateInfoCall = stateInfoEndpoint.getStateInfo()
        stateInfoCall.enqueue(object : Callback<DeviceStateInfo> {
            override fun onResponse(call: Call<DeviceStateInfo>, response: Response<DeviceStateInfo>) = onSuccess(device, call, response)
            override fun onFailure(call: Call<DeviceStateInfo>, t: Throwable) = onFailure(device, call, t)
        })
    }

    private fun onFailure(device: DeviceItem, call: Call<DeviceStateInfo>, t: Throwable) {
        Log.e(TAG, t.message!!)
        device.isOnline = false
        DeviceRepository.update(device)
    }

    private fun onSuccess(device: DeviceItem, call: Call<DeviceStateInfo>, response: Response<DeviceStateInfo>) {
        if (response.code() == 200) {
            val deviceStateInfo = response.body()!!

            device.isOnline = true
            if (!device.isCustomName) {
                device.name = deviceStateInfo.info.name
            }

            DeviceRepository.update(device)
        }
    }
}
