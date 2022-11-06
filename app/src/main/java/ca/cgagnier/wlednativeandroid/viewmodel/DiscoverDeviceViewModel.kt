package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.*
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import kotlinx.coroutines.launch

class DiscoverDeviceViewModel(private val repository: DeviceRepository): ViewModel() {
    private val _allDevicesAddresses = MutableLiveData<ArrayList<String>>()

    val allDevices: LiveData<List<Device>> get() = Transformations.switchMap(_allDevicesAddresses) {
            addresses -> repository.findDevicesWithAddresses(addresses).asLiveData()
    }

    fun insert(device: Device) = viewModelScope.launch {
        val addressesList: ArrayList<String> = if (_allDevicesAddresses.value != null) {
            ArrayList(_allDevicesAddresses.value!!)
        } else {
            ArrayList()
        }

        addressesList.add(device.address)
        _allDevicesAddresses.value = addressesList
    }

    fun clear() = viewModelScope.launch {
        _allDevicesAddresses.value = ArrayList()
    }
}

class DiscoverDeviceViewModelFactory(private val repository: DeviceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoverDeviceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoverDeviceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
