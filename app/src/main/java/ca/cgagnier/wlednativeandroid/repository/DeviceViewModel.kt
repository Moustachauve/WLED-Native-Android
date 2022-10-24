package ca.cgagnier.wlednativeandroid.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.cgagnier.wlednativeandroid.DeviceItem

class DeviceViewModel : ViewModel() {
    private var _currentDevice = MutableLiveData<DeviceItem>()
    val currentDevice: LiveData<DeviceItem> get() = _currentDevice

    init {
        // TODO open either last opened or first online device
        _currentDevice.value = DeviceRepository.getAllNotHidden()[0]
    }

    fun updateCurrentDevice(device: DeviceItem) {
        _currentDevice.value = device
    }
}