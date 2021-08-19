package ca.cgagnier.wlednativeandroid.service.api

import ca.cgagnier.wlednativeandroid.model.DeviceStateInfo
import retrofit2.Call
import retrofit2.http.GET

interface StateInfo {

    @GET("json/si")
    fun getStateInfo(): Call<DeviceStateInfo>
}