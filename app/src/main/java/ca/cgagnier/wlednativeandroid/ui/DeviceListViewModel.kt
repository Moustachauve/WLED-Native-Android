package ca.cgagnier.wlednativeandroid.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DeviceListViewModel"

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val repository: DeviceRepository,
    private val stateFactory: StateFactory
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
    val deviceListUiState: StateFlow<DeviceListUiState> = repository.allVisibleDevicesOfflineLast.map {
        DeviceListUiState(it)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = DeviceListUiState()
    )
    var isPolling by mutableStateOf(false)
        private set
    private var job: Job? = null

    fun startRefreshDevicesLoop() {
        if (isPolling) {
            return
        }
        isPolling = true
        // If there's an existing job that's not registered, kill it.
        job?.cancel()
        Log.i(TAG, "Starting refresh devices loop")
        job = viewModelScope.launch(Dispatchers.IO) {
            while (isPolling) {
                Log.i(TAG, "Looping refreshes")
                refreshDevices(silent = true)
                delay(10000)
            }
        }
    }
    fun stopRefreshDevicesLoop() {
        Log.i(TAG, "Stopping refresh devices loop")
        job?.cancel()
        isPolling = false
        job = null
    }

    fun refreshDevices(silent: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "Refreshing devices")
            val devices = repository.getAllDevices()
            Log.d(TAG, "devices found: ${devices.size ?: 0}")
            devices.map { device ->
                refreshDevice(device, silent)
            }
        }
    }

    private suspend fun refreshDevice(device: Device, silent: Boolean) {
        Log.d(TAG, "Refreshing device ${device.name} - ${device.address}")
        stateFactory.getState(device).requestsManager.addRequest(
            RefreshRequest(
                device,
                silentRefresh = silent,
            )
        )
    }

    private suspend fun findWithSameMacAddress(device: Device): Device? {
        return repository.findDeviceByMacAddress(device.macAddress)
    }

    fun insert(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Inserting device ${device.name} - ${device.address}")
        repository.insert(device)
    }

    fun delete(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Deleting device ${device.name} - ${device.address}")
        repository.delete(device)
    }
}


data class DeviceListUiState(
    val devices: List<Device> = listOf()
)
