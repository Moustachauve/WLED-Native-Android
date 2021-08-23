package ca.cgagnier.wlednativeandroid.service.api

import ca.cgagnier.wlednativeandroid.model.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.JsonPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface JsonApi {
    @GET("json/si")
    fun getStateInfo(): Call<DeviceStateInfo>

    @POST("json")
    fun postJson(@Body requestBody: JsonPost): Call<DeviceStateInfo>
}