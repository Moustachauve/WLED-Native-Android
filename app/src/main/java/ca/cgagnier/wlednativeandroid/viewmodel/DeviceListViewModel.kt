package ca.cgagnier.wlednativeandroid.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val repository: DeviceRepository,
    //userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
/*
    @OptIn(ExperimentalCoroutinesApi::class)
    val allDevices: LiveData<List<Device>> = userPreferencesRepository.showOfflineDevicesLast.flatMapLatest { showOfflineLast ->
        if (showOfflineLast) {
            repository.allVisibleDevicesOfflineLast
        } else {
            repository.allVisibleDevices
        }
    }.asLiveData()
*/
    val allDevicesFlow = repository.allVisibleDevicesOfflineLast

    var selectedDevice: Device? = null
    var isTwoPane = MutableLiveData(false)
    var isListHidden = MutableLiveData(false)

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

    companion object {
        const val TAG = "DeviceListViewModel"
    }
}