package ca.cgagnier.wlednativeandroid.service.api

import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
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