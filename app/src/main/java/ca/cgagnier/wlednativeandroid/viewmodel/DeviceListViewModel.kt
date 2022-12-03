package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.*
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeviceListViewModel(private val repository: DeviceRepository,
    private val userPreferencesRepository: UserPreferencesRepository): ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val allDevices: LiveData<List<Device>> = userPreferencesRepository.showOfflineDevicesLast.flatMapLatest { showOfflineLast ->
        if (showOfflineLast) {
            repository.allVisibleDevicesOfflineLast
        } else {
            repository.allVisibleDevices
        }
    }.asLiveData()

    val activeDevice: LiveData<Device?> = getActiveDevice().asLiveData()

    var isTwoPane = MutableLiveData(false)
    var expectDeviceChange = false

    fun insert(device: Device) = viewModelScope.launch {
        repository.insert(device)
    }

    fun delete(device: Device) = viewModelScope.launch {
        repository.delete(device)
    }

    fun contains(device: Device): Boolean {
        return repository.contains(device)
    }

    suspend fun findWithSameMacAddress(device: Device): Device? {
        return repository.findDeviceByMacAddress(device.macAddress)
    }

    fun updateActiveDevice(device: Device) = viewModelScope.launch {
        expectDeviceChange = true
        userPreferencesRepository.updateSelectedDevice(device)
    }

    private fun getActiveDevice(): Flow<Device?> {
        return userPreferencesRepository.selectedDeviceAddress.map {
            var device: Device? = null
            if (it == DeviceDiscovery.DEFAULT_WLED_AP_IP) {
                device = DeviceDiscovery.getDefaultAPDevice()
            } else if (it != "") {
                device = repository.findDeviceByAddress(it)
            }
            if (device == null && allDevices.value?.isNotEmpty() == true) {
                device = allDevices.value?.first()
            }
            device
        }
    }
}

class DeviceListViewModelFactory(
        private val repository: DeviceRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceListViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
