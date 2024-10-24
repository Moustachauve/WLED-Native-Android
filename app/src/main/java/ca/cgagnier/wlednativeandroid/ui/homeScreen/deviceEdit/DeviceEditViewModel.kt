package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceEdit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
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
    private val versionWithAssetsRepository: VersionWithAssetsRepository
) : ViewModel() {

    private var _updateDetailsVersion: MutableStateFlow<VersionWithAssets?> = MutableStateFlow(null)
    val updateDetailsVersion = _updateDetailsVersion.asStateFlow()

    private var _updateInstallVersion: MutableStateFlow<VersionWithAssets?> = MutableStateFlow(null)
    val updateInstallVersion = _updateInstallVersion.asStateFlow()

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

    fun updateDeviceBranch(device: Device, branch: Branch) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "updateDeviceHidden: ${device.name}, updateChannel: $branch")
        repository.update(
            device.copy(
                branch = branch
            )
        )
    }

    fun showUpdateDetails(device: Device) = viewModelScope.launch(Dispatchers.IO) {
        val version = device.newUpdateVersionTagAvailable
        _updateDetailsVersion.value = versionWithAssetsRepository.getVersionByTag(version)
    }

    fun hideUpdateDetails() {
        _updateDetailsVersion.value = null
    }

    fun startUpdateInstall(version: VersionWithAssets) {
        _updateInstallVersion.value = version
    }

    fun stopUpdateInstall() {
        _updateInstallVersion.value = null
    }

}