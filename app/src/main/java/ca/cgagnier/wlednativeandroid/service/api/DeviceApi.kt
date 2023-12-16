package ca.cgagnier.wlednativeandroid.service.api

import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.model.wledapi.State
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DeviceApi {
    @GET("json/si")
    fun getStateInfo(): Call<DeviceStateInfo>

    @POST("json/state")
    fun postJson(@Body requestBody: JsonPost): Call<State>

    @Multipart
    @POST("update")
    fun updateDevice(
        @Part binaryFile: MultipartBody.Part
    ): Call<ResponseBody>
}