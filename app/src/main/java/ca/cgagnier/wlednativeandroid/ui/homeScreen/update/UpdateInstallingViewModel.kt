package ca.cgagnier.wlednativeandroid.ui.homeScreen.update

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.device.api.request.SoftwareUpdateRequest
import ca.cgagnier.wlednativeandroid.service.update.DeviceUpdateService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import java.io.File
import javax.inject.Inject

private const val TAG = "UpdateInstallingViewModel"

@HiltViewModel
class UpdateInstallingViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val stateFactory: StateFactory,
): ViewModel() {
    private var updateStarted = false

    private var _state = MutableStateFlow(UpdateInstallingState())
    val state = _state.asStateFlow()

    private var _device: MutableStateFlow<Device?> = MutableStateFlow(null)
    val device = _device.asStateFlow()

    private var _version: MutableStateFlow<VersionWithAssets?> = MutableStateFlow(null)
    val version = _version.asStateFlow()

    fun toggleErrorMessage() {
        if (state.value.step !is UpdateInstallingStep.Error) {
            return
        }
        _state.update { previousState ->
            val step = previousState.step as UpdateInstallingStep.Error
            previousState.copy(
                step = UpdateInstallingStep.Error(
                    step.error,
                    !step.showError
                )
            )
        }
    }

    fun resetState() {
        // Reset on dismiss so we can try again next time
        updateStarted = false
    }

    fun startUpdate(
        device: Device,
        version: VersionWithAssets,
        cacheDir: File,
    ) {
        if (updateStarted) {
            Log.w(TAG, "Update already started, ignoring startUpdate for ${device.name}")
            return
        }
        updateStarted = true
        Log.i(TAG, "startUpdate for device ${device.name}")
        _device.update { device }
        _version.update { version }
        _state.update { previousState ->
            previousState.copy(
                canDismiss = true,
                step = UpdateInstallingStep.Starting
            )
        }

        val updateService = DeviceUpdateService(device, version, cacheDir)
        if (!updateService.couldDetermineAsset()) {
            _state.update { previousState ->
                previousState.copy(
                    canDismiss = true,
                    step = UpdateInstallingStep.NoCompatibleVersion,
                    assetName = updateService.getAssetName()
                )
            }
            return
        }

        downloadAsset(updateService)
    }

    private fun downloadAsset(
        updateService: DeviceUpdateService
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (updateService.isAssetFileCached()) {
            Log.d(TAG, "asset '${updateService.getAssetName()}' is already downloaded, reusing")
            installUpdate(updateService)
            return@launch
        }

        Log.d(TAG, "Downloading asset '${updateService.getAssetName()}'")
        updateService.downloadBinary().collect { downloadState ->
            when (downloadState) {
                is DownloadState.Downloading -> {
                    Log.d(TAG, "File download Progress=${downloadState.progress}")
                    _state.update { previousState ->
                        previousState.copy(
                            canDismiss = true,
                            step = UpdateInstallingStep.Downloading(downloadState.progress),
                            assetName = updateService.getAssetName()
                        )
                    }
                }

                is DownloadState.Failed -> {
                    Log.e(TAG, "File download Fail: ${downloadState.error}")
                    _state.update { previousState ->
                        previousState.copy(
                            canDismiss = true,
                            step = UpdateInstallingStep.Error(
                                downloadState.error.toString()
                            ),
                            assetName = updateService.getAssetName()
                        )
                    }
                    this.coroutineContext.job.cancel()
                }

                is DownloadState.Finished -> {
                    Log.d(TAG, "File download Finished")
                    installUpdate(updateService)
                    this.coroutineContext.job.cancel()
                }
            }
        }
    }

    private fun installUpdate(updateService: DeviceUpdateService) {
        Log.d(TAG, "Uploading binary '${updateService.getAssetName()}' to device")
        _state.update { previousState ->
            previousState.copy(
                canDismiss = false,
                step = UpdateInstallingStep.Installing,
                assetName = updateService.getAssetName()
            )
        }
        stateFactory.getState(updateService.device).requestsManager.addRequest(
            SoftwareUpdateRequest(
                updateService.device,
                updateService.getPathForAsset(),
                callback = { onSoftwareUpdateResponse(it) },
                errorCallback = { onSoftwareUpdateError(it) },
            )
        )
    }

    private fun onSoftwareUpdateResponse(response: Response<ResponseBody>) {
        if (response.code() in 200..299) {
            _state.update { previousState ->
                previousState.copy(
                    canDismiss = true,
                    step = UpdateInstallingStep.Done
                )
            }
        } else {
            Log.d(TAG, "OTA Failed, code ${response.code()}")
            val errorString = "${response.code()}: ${getHtmlErrorMessage(response)}"
            Log.d(TAG, "OTA Failed onResponse, error $errorString")
            _state.update { previousState ->
                previousState.copy(
                    canDismiss = true,
                    step = UpdateInstallingStep.Error(errorString)
                )
            }
        }
        updateDeviceUpdated()
    }

    private fun onSoftwareUpdateError(e: Exception) {
        Log.d(TAG, "OTA Failed, call failed")
        Log.e(TAG, e.toString())
        val errorString = e.toString()
        Log.d(TAG, "OTA Failed onFailure, error $errorString")
        _state.update { previousState ->
            previousState.copy(
                canDismiss = true,
                step = UpdateInstallingStep.Error(errorString)
            )
        }
    }

    private fun updateDeviceUpdated() = viewModelScope.launch(Dispatchers.IO) {
        val device = device.value ?: return@launch
        val version = version.value ?: return@launch
        Log.d(TAG, "Saving deviceUpdated")
        val updatedDevice = device.copy(
            version = version.version.tagName.drop(1),
            newUpdateVersionTagAvailable = ""
        )
        deviceRepository.update(updatedDevice)
        stateFactory.getState(device).requestsManager.addRequest(RefreshRequest(device))
    }

    private fun getHtmlErrorMessage(response: Response<ResponseBody>): String {
        val bodyHtml = Jsoup.parseBodyFragment(
            response.body()?.string() ?: response.errorBody()?.string() ?: ""
        )
        bodyHtml.select("title").remove()
        bodyHtml.select("button").remove()
        bodyHtml.select("h1").remove()
        return bodyHtml.text()
    }
}