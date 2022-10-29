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
    var isTwoPane = false

    init {
        var selectedIndex = OptionsRepository.get().lastSelectedIndex
        if (DeviceRepository.getAllNotHidden().isEmpty()) {
            selectedIndex = -1
        } else if (selectedIndex < 0 || selectedIndex >= DeviceRepository.getAllNotHidden().count()) {
            selectedIndex = 0
            OptionsRepository.saveSelectedIndex(selectedIndex)
        }
        _currentSelectedIndex.value = selectedIndex
        if (selectedIndex > 0) {
            _currentDevice.value = DeviceRepository.getAllNotHidden()[selectedIndex]
        }
    }

    fun updateSelectedIndex(index: Int) {
        _currentSelectedIndex.value = index
        OptionsRepository.saveSelectedIndex(index)
    }

    fun updateCurrentDevice(device: DeviceItem) {
        _currentDevice.value = device
    }
}