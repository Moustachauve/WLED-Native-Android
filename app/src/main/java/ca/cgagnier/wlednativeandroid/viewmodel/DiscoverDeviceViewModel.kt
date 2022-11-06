package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.*
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import kotlinx.coroutines.launch

class DiscoverDeviceViewModel(private val repository: DeviceRepository): ViewModel() {

    // TODO Keep only list of addresses and do a query to the database WHERE address IN (?)
    //      in order to keep the data in sync (change of name, etc)
    private val _allDevices = MutableLiveData<ArrayList<Device>>()
    val allDevices: LiveData<ArrayList<Device>> get() = _allDevices


    fun insert(device: Device) = viewModelScope.launch {
        val devicesList: ArrayList<Device>
        devicesList = if (_allDevices.value != null) {
            ArrayList(_allDevices.value!!)
        } else {
            ArrayList()
        }

        devicesList.add(device)
        _allDevices.value = devicesList
    }

    fun clear() = viewModelScope.launch {
        _allDevices.value =  ArrayList()
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
