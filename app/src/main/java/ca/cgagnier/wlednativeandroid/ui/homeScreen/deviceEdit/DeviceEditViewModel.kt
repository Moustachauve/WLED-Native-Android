package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceEdit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "DeviceEditViewModel"

@HiltViewModel
class DeviceEditViewModel @Inject constructor(
    private val repository: DeviceRepository,
    private val versionWithAssetsRepository: VersionWithAssetsRepository,
    private val stateFactory: StateFactory,
) : ViewModel() {

    private var _updateDetailsVersion: MutableStateFlow<VersionWithAssets?> = MutableStateFlow(null)
    val updateDetailsVersion = _updateDetailsVersion.asStateFlow()

    private var _updateInstallVersion: MutableStateFlow<VersionWithAssets?> = MutableStateFlow(null)
    val updateInstallVersion = _updateInstallVersion.asStateFlow()

    private var _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates = _isCheckingUpdates.asStateFlow()

    fun updateCustomName(device: Device, name: String) = viewModelScope.launch(Dispatchers.IO) {
        val isCustomName = name != ""
        val updatedDevice = device.copy(
            name = name,
            isCustomName = isCustomName
        )

        Log.d(TAG, "updateCustomName: $name, isCustom: $isCustomName")

        repository.update(updatedDevice)
    }

    fun updateDeviceHidden(device: Device, isHidden: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "updateDeviceHidden: ${device.name}, isHidden: $isHidden")
        repository.update(
            device.copy(
                isHidden = isHidden
            )
        )
    }

    fun updateDeviceBranch(device: Device, branch: Branch, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "updateDeviceHidden: ${device.name}, updateChannel: $branch")
        val updatedDevice = device.copy(
            branch = branch
        )
        repository.update(updatedDevice)
        checkForUpdates(updatedDevice, context)
    }

    fun showUpdateDetails(device: Device) = viewModelScope.launch(Dispatchers.IO) {
        val version = device.newUpdateVersionTagAvailable
        _updateDetailsVersion.value = versionWithAssetsRepository.getVersionByTag(version)
    }

    fun hideUpdateDetails() {
        _updateDetailsVersion.value = null
    }

    fun skipUpdate(device: Device, version: VersionWithAssets) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Saving skipUpdateTag")
        val updatedDevice = device.copy(
            newUpdateVersionTagAvailable = "",
            skipUpdateTag = version.version.tagName
        )
        repository.update(updatedDevice)
        _updateDetailsVersion.value = null
    }

    fun startUpdateInstall(version: VersionWithAssets) {
        _updateInstallVersion.value = version
    }

    fun stopUpdateInstall() {
        _updateInstallVersion.value = null
    }

    fun checkForUpdates(device: Device, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        _isCheckingUpdates.value = true
        val updatedDevice = removeSkipVersion(device)
        val releaseService = ReleaseService(versionWithAssetsRepository)
        releaseService.refreshVersions(context.cacheDir)
        stateFactory.getState(updatedDevice).requestsManager.addRequest(
            RefreshRequest(updatedDevice, callback = {
                _isCheckingUpdates.value = false
            })
        )
    }

    private suspend fun removeSkipVersion(device: Device): Device {
        val updatedDevice = device.copy(skipUpdateTag = "")
        repository.update(updatedDevice)
        return updatedDevice
    }

}