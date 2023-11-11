package ca.cgagnier.wlednativeandroid.viewmodel

import android.util.Log
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
    var doRefreshWeb = MutableLiveData(false)
    var expectDeviceChange = true

    fun insert(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Inserting device")
        repository.insert(device)
    }

    fun update(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Updating device")
        repository.update(device)
    }

    fun delete(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Deleting device")
        repository.delete(device)
    }

    fun contains(device: Device): Boolean {
        return repository.contains(device)
    }

    suspend fun findWithSameMacAddress(device: Device): Device? {
        return repository.findDeviceByMacAddress(device.macAddress)
    }

    fun updateActiveDevice(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Update active device")
        expectDeviceChange = true
        userPreferencesRepository.updateSelectedDevice(device)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getActiveDevice(): Flow<Device?> {
        return userPreferencesRepository.selectedDeviceAddress.flatMapLatest {
            Log.d(TAG, "selectedDeviceAddress changed")
            var device: Flow<Device?> = flow { emit(null) }
            if (it == DeviceDiscovery.DEFAULT_WLED_AP_IP) {
                device = flow { emit(DeviceDiscovery.getDefaultAPDevice()) }
            } else if (it != "") {
                device = repository.findLiveDeviceByAddress(it)
            }
            if (device.first() == null && allDevices.value?.isNotEmpty() == true) {
                device = repository.findFirstLiveDevice()
            }
            device
        }
    }

    companion object {
        const val TAG = "DeviceListViewModel"
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
