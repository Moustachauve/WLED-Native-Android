package ca.cgagnier.wlednativeandroid.service.api

import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.model.wledapi.State
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DeviceApi {
    @GET("json/si")
    suspend fun getStateInfo(): Response<DeviceStateInfo>

    @POST("json/state")
    suspend fun postJson(@Body requestBody: JsonPost): Response<State>

    @Multipart
    @POST("update")
    suspend fun updateDevice(
        @Part binaryFile: MultipartBody.Part
    ): Response<ResponseBody>
}