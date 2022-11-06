package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.*
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import kotlinx.coroutines.launch

class ManageDevicesViewModel(private val repository: DeviceRepository): ViewModel() {
    val allDevices: LiveData<List<Device>> = repository.allDevices.asLiveData()

    private var _activeDevice = MutableLiveData<Device>()
    val activeDevice: LiveData<Device> get() = _activeDevice

    fun insert(device: Device) = viewModelScope.launch {
        repository.insert(device)
    }

    fun delete(device: Device) = viewModelScope.launch {
        repository.delete(device)
    }

    fun updateActiveDevice(device: Device) {
        _activeDevice.value = device
    }
}

class ManageDevicesViewModelFactory(private val repository: DeviceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageDevicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageDevicesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
