package ca.cgagnier.wlednativeandroid.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.cgagnier.wlednativeandroid.DeviceItem

class DeviceViewModel : ViewModel() {
    private var _currentSelectedIndex = MutableLiveData<Int>()
    private var _currentDevice = MutableLiveData<DeviceItem>()
    val currentSelectedIndex: LiveData<Int> get() = _currentSelectedIndex
    val currentDevice: LiveData<DeviceItem> get() = _currentDevice

    init {
        // TODO open either last opened or first online device
        val selectedIndex = 0
        _currentSelectedIndex.value = selectedIndex
        _currentDevice.value = DeviceRepository.getAllNotHidden()[selectedIndex]
    }

    fun updateSelectedIndex(index: Int) {
        _currentSelectedIndex.value = index
    }

    fun updateCurrentDevice(device: DeviceItem) {
        _currentDevice.value = device
    }
}