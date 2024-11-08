package ca.cgagnier.wlednativeandroid.service.device.api

import android.graphics.Color
import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.model.wledapi.State
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.api.DeviceApi
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.SoftwareUpdateRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.StateChangeRequest
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class JsonApiRequestHandler @Inject constructor(
    private var deviceRepository: DeviceRepository,
    private var releaseService: ReleaseService
) : RequestHandler() {
    private fun getJsonApi(device: Device): DeviceApi {
        return Retrofit.Builder().baseUrl(device.getDeviceUrl())
            .addConverterFactory(MoshiConverterFactory.create()).build()
            .create(DeviceApi::class.java)
    }

    override suspend fun handleRefreshRequest(request: RefreshRequest) {
        if (!request.silentRefresh) {
            val newDevice = request.device.copy(isRefreshing = true)
            Log.d(TAG, "[${request.device.address}] Saving non-silent update")
            deviceRepository.update(newDevice)
        }

        val response = try {
            getJsonApi(request.device).getStateInfo()
        } catch (e: Exception) {
            val newDevice = onFailure(request.device, e)
            request.callback?.invoke(newDevice)
            return
        }

        if (response.code() in 200..299 && response.isSuccessful && response.body() != null) {
            try {
                val newDevice = updateDevice(
                    request.device, response, request.saveChanges
                )
                request.callback?.invoke(newDevice)
            } catch (e: Exception) {
                Log.e(TAG, "[${request.device.address}] Exception when parsing success callback")
                request.callback?.invoke(request.device)
            }
        } else {
            val newDevice = onFailure(
                request.device, Exception("Response success, but not valid")
            )
            request.callback?.invoke(newDevice)
        }
    }

    override suspend fun handleChangeStateRequest(request: StateChangeRequest) {
        Log.d(TAG, "[${request.device.address}] Posting update to device")

        val response = try {
            getJsonApi(request.device).postJson(request.state)
        } catch (e: Exception) {
            onFailure(request.device, e)
            return
        }

        if (response.code() in 200..299 && response.isSuccessful && response.body() != null) {
            try {
                updateDevice(request.device, response, request.saveChanges)
            } catch (e: Exception) {
                Log.e(TAG, "[${request.device.address}] Exception when parsing post response")
            }
        } else {
            onFailure(request.device, Exception("Response success, but not valid"))
        }
    }

    override suspend fun handleSoftwareUpdateRequest(request: SoftwareUpdateRequest) {
        try {
            val reqFile =
                request.binaryFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val response = getJsonApi(request.device).updateDevice(
                MultipartBody.Part.createFormData("file", "binary", reqFile)
            )
            request.callback?.invoke(response)
        } catch (e: Exception) {
            request.errorCallback?.invoke(e)
        }
    }

    // TODO: device update logic should not be tied to the device API.
    private suspend fun updateDevice(
        device: Device, response: Response<DeviceStateInfo>, saveChanges: Boolean
    ): Device {
        val deviceStateInfo = response.body()!!

        var color = Color.WHITE
        if (!deviceStateInfo.state.segment.isNullOrEmpty()) {
            val colors = deviceStateInfo.state.segment[0].colors
            if (!colors.isNullOrEmpty()) {
                val colorInfo = colors[0]
                color = if (colorInfo.size in 3..4) Color.rgb(
                    colorInfo[0], colorInfo[1], colorInfo[2]
                ) else Color.WHITE
            }
        }

        var branch = device.branch
        if (branch == Branch.UNKNOWN) {
            branch = if (device.version.contains("-b")) Branch.BETA else Branch.STABLE
        }

        val deviceVersion = deviceStateInfo.info.version ?: Device.UNKNOWN_VALUE
        val updateVersionTagAvailable = releaseService.getNewerReleaseTag(
            deviceVersion, branch, device.skipUpdateTag
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
            batteryPercentage = (deviceStateInfo.info.usermods.batLevel?.get(0) as? Int) ?: 0,
            hasBattery = (deviceStateInfo.info.usermods.batLevel != null),

        )
        if (saveChanges && updatedDevice != device) {
            Log.d(TAG, "[${updatedDevice.address}] Saving update of device from API")
            deviceRepository.update(updatedDevice)
        }

        return updatedDevice
    }

    @JvmName("updateDeviceFromState")
    private suspend fun updateDevice(
        device: Device, response: Response<State>, saveChanges: Boolean
    ): Device {
        val state = response.body()!!

        var color = Color.WHITE
        if (!state.segment.isNullOrEmpty()) {
            val colors = state.segment[0].colors
            if (!colors.isNullOrEmpty()) {
                val colorInfo = colors[0]
                color = if (colorInfo.size in 3..4) Color.rgb(
                    colorInfo[0], colorInfo[1], colorInfo[2]
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
            Log.d(TAG, "[${updatedDevice.address}] Saving update of device from post API")
            deviceRepository.update(updatedDevice)
        }

        return updatedDevice
    }

    private suspend fun onFailure(
        device: Device, t: Throwable? = null
    ): Device {
        if (t != null) {
            Log.e(TAG, t.message!!)
        }
        val updatedDevice = device.copy(isOnline = false, isRefreshing = false)

        Log.d(TAG, "[${updatedDevice.address}] Saving device API onFailure: ${device.name}")
        deviceRepository.update(updatedDevice)
        return updatedDevice
    }

    companion object {
        private const val TAG = "JsonApiRequestHandler"
    }
}