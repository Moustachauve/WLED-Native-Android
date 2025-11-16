package ca.cgagnier.wlednativeandroid.service.device.api.request

import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

class SoftwareUpdateRequest(
    device: StatefulDevice,
    val binaryFile: File,
    val callback: ((Response<ResponseBody>) -> Unit)? = null,
    val errorCallback: ((Exception) -> Unit)? = null
) : Request(device)