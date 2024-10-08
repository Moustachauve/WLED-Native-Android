package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceEdit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "DeviceEditViewModel"

@HiltViewModel
class DeviceEditViewModel @Inject constructor(
    private val repository: DeviceRepository
) : ViewModel() {
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

}