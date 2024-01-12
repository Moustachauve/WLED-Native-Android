package ca.cgagnier.wlednativeandroid.service

import android.graphics.Color
import android.util.Log
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.model.wledapi.State
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.api.DeviceApi
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

class DeviceApiService(
    private var deviceRepository: DeviceRepository,
    private var releaseService: ReleaseService
) {

    @OptIn(DelicateCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext(TAG))

    fun update(
        device: Device,
        silentUpdate: Boolean,
        saveChanges: Boolean = true,
        callback: ((Device) -> Unit)? = null
    ) {
        if (!silentUpdate) {
            val newDevice = device.copy(isRefreshing = true)

            scope.launch {
                Log.d(TAG, "Saving non-silent update")
                deviceRepository.update(newDevice)
            }
        }

        val stateInfoCall: Call<DeviceStateInfo>
        try {
            stateInfoCall = getJsonApi(device).getStateInfo()
        } catch (e: IllegalArgumentException) {
            Log.wtf(TAG, "Device has invalid address: " + device.address)
            Firebase.crashlytics.recordException(e)
            scope.launch {
                deviceRepository.delete(device)
            }
            return
        }

        stateInfoCall.enqueue(object : Callback<DeviceStateInfo> {
            override fun onResponse(
                call: Call<DeviceStateInfo>,
                response: Response<DeviceStateInfo>
            ) {
                if (response.code() in 200..299 && response.isSuccessful && response.body() != null) {
                    try {
                        updateDevice(device, response, saveChanges, callback)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception when parsing success callback")
                        Firebase.crashlytics.log("Response success, but parsing result failed")
                        Firebase.crashlytics.setCustomKey("response code", response.code())
                        Firebase.crashlytics.setCustomKey("response isSuccessful", response.isSuccessful)
                        Firebase.crashlytics.setCustomKey(
                            "response errorBody",
                            response.errorBody().toString()
                        )
                        Firebase.crashlytics.setCustomKey(
                            "exception message",
                            e.message ?: "[No Message]"
                        )
                        Firebase.crashlytics.setCustomKey("response headers", response.headers().toString())
                        Firebase.crashlytics.recordException(e)
                    }
                } else {
                    Firebase.crashlytics.log("Response success, but not valid")
                    Firebase.crashlytics.setCustomKey("response code", response.code())
                    Firebase.crashlytics.setCustomKey("response isSuccessful", response.isSuccessful)
                    Firebase.crashlytics.setCustomKey(
                        "response errorBody",
                        response.errorBody().toString()
                    )
                    Firebase.crashlytics.setCustomKey("response headers", response.headers().toString())
                    onFailure(device, Exception("Response success, but not valid"), callback = callback)
                }
            }

            override fun onFailure(call: Call<DeviceStateInfo>, t: Throwable) =
                onFailure(device, t, callback)
        })
    }

    fun postJson(device: Device, jsonData: JsonPost, saveChanges: Boolean = true) {
        Log.d(TAG, "Posting update to device [${device.address}]")

        val stateInfoCall = getJsonApi(device).postJson(jsonData)
        stateInfoCall.enqueue(object : Callback<State> {
            override fun onResponse(
                call: Call<State>,
                response: Response<State>
            ) {
                if (response.code() in 200..299 && response.isSuccessful && response.body() != null) {
                    try {
                        updateDevice(device, response, saveChanges)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception when parsing post response")
                        Firebase.crashlytics.log("Response success, but parsing result failed")
                        Firebase.crashlytics.setCustomKey("response code", response.code())
                        Firebase.crashlytics.setCustomKey("response isSuccessful", response.isSuccessful)
                        Firebase.crashlytics.setCustomKey(
                            "response errorBody",
                            response.errorBody().toString()
                        )
                        Firebase.crashlytics.setCustomKey(
                            "exception message",
                            e.message ?: "[No Message]"
                        )
                        Firebase.crashlytics.setCustomKey("response headers", response.headers().toString())
                        Firebase.crashlytics.recordException(e)
                    }
                } else {
                    Firebase.crashlytics.log("Response success, but not valid")
                    Firebase.crashlytics.setCustomKey("response code", response.code())
                    Firebase.crashlytics.setCustomKey("response isSuccessful", response.isSuccessful)
                    Firebase.crashlytics.setCustomKey(
                        "response errorBody",
                        response.errorBody().toString()
                    )
                    Firebase.crashlytics.setCustomKey("response headers", response.headers().toString())
                    onFailure(device, Exception("Response success, but not valid"))
                }
            }

            override fun onFailure(call: Call<State>, t: Throwable) =
                onFailure(device, t)
        })
    }

    private fun getJsonApi(device: Device): DeviceApi {
        return Retrofit.Builder()
            .baseUrl(device.getDeviceUrl())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(DeviceApi::class.java)
    }

    private fun onFailure(
        device: Device,
        t: Throwable? = null,
        callback: ((Device) -> Unit)? = null
    ) {
        if (t != null) {
            Firebase.crashlytics.recordException(t)
            Log.e(TAG, t.message!!)
        }
        val updatedDevice = device.copy(isOnline = false, isRefreshing = false)
        if (callback != null) {
            callback(updatedDevice)
            return
        }
        scope.launch {
            Log.d(TAG, "Saving device API onFailure")
            deviceRepository.update(updatedDevice)
        }
    }

    fun updateDevice(
        device: Device,
        response: Response<DeviceStateInfo>,
        saveChanges: Boolean,
        callback: ((Device) -> Unit)? = null
    ) {
        scope.launch {
            val deviceStateInfo = response.body()!!

            var color = Color.WHITE
            if (!deviceStateInfo.state.segment.isNullOrEmpty()) {
                val colors = deviceStateInfo.state.segment[0].colors
                if (!colors.isNullOrEmpty()) {
                    val colorInfo = colors[0]
                    color = if (colorInfo.size in 3..4) Color.rgb(
                        colorInfo[0],
                        colorInfo[1],
                        colorInfo[2]
                    ) else Color.WHITE
                }
            }

            var branch = device.branch
            if (branch == Branch.UNKNOWN) {
                branch = if (device.version.contains("-b")) Branch.BETA else Branch.STABLE
            }

            val deviceVersion = deviceStateInfo.info.version ?: Device.UNKNOWN_VALUE
            val updateVersionTagAvailable =
                releaseService.getNewerReleaseTag(
                    deviceVersion,
                    branch,
                    device.skipUpdateTag
                )

            val updatedDevice = device.copy(
                macAddress = deviceStateInfo.info.mac ?: Device.UNKNOWN_VALUE,
                isOnline = true,
                name = if (device.isCustomName) device.name else deviceStateInfo.info.name,
                brightness = if (device.isSliding) device.brightness else deviceStateInfo.state.brightness,
                isPoweredOn = deviceStateInfo.state.isOn,
                color = color,
                isRefreshing = false,
                networkRssi = deviceStateInfo.info.wifi.rssi ?: 0,
                isEthernet = false,
                platformName = deviceStateInfo.info.platformName ?: Device.UNKNOWN_VALUE,
                version = deviceVersion,
                newUpdateVersionTagAvailable = updateVersionTagAvailable,
                branch = branch,
                brand = deviceStateInfo.info.brand ?: Device.UNKNOWN_VALUE,
                productName = deviceStateInfo.info.product ?: Device.UNKNOWN_VALUE,
            )

            if (saveChanges && updatedDevice != device) {
                Log.d(TAG, "Saving update of device from API")
                deviceRepository.update(updatedDevice)
            }

            if (callback != null) {
                callback(updatedDevice)
                return@launch
            }
        }
    }

    fun updateDevice(
        device: Device,
        response: Response<State>,
        saveChanges: Boolean
    ) {
        scope.launch {
            val state = response.body()!!

            var color = Color.WHITE
            if (!state.segment.isNullOrEmpty()) {
                val colors = state.segment[0].colors
                if (!colors.isNullOrEmpty()) {
                    val colorInfo = colors[0]
                    color = if (colorInfo.size in 3..4) Color.rgb(
                        colorInfo[0],
                        colorInfo[1],
                        colorInfo[2]
                    ) else Color.WHITE
                }
            }

            val updatedDevice = device.copy(
                isOnline = true,
                brightness = if (device.isSliding) device.brightness else state.brightness,
                isPoweredOn = state.isOn,
                color = color,
                isRefreshing = false,
            )

            if (saveChanges && updatedDevice != device) {
                Log.d(TAG, "Saving update of device from post API")
                deviceRepository.update(updatedDevice)
            }
        }
    }

    fun installUpdate(device: Device, binaryFile: File): Call<ResponseBody> {
        val reqFile = binaryFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        return getJsonApi(device).updateDevice(
            MultipartBody.Part.createFormData("file", "binary", reqFile)
        )
    }

    companion object {
        private const val TAG = "DeviceApi"

        fun fromApplication(application: DevicesApplication): DeviceApiService {
            return DeviceApiService(
                application.deviceRepository,
                ReleaseService(application.versionWithAssetsRepository)
            )
        }
    }
}
