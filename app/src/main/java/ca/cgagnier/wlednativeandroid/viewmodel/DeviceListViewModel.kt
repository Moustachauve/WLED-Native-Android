package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.*
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DeviceListViewModel(private val repository: DeviceRepository,
    private val userPreferencesRepository: UserPreferencesRepository): ViewModel() {

    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    val allDevices: LiveData<List<Device>> = repository.allVisibleDevices.asLiveData()

    val activeDevice: LiveData<Device?> = getActiveDevice().asLiveData()

    var isTwoPane = MutableLiveData(false)

    fun insert(device: Device) = viewModelScope.launch {
        repository.insert(device)
    }

    fun contains(device: Device): LiveData<Boolean> {
        return repository.contains(device)
    }

    fun updateActiveDevice(device: Device) = viewModelScope.launch {
        userPreferencesRepository.updateSelectedDevice(device)
    }

    private fun getActiveDevice(): Flow<Device?> {
        return userPreferencesFlow.map {
            val selectedAddress = it.selectedDeviceAddress ?: ""
            var device: Device? = null
            if (selectedAddress != "") {
                device = repository.findDeviceByAddress(selectedAddress)
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
