package ca.cgagnier.wlednativeandroid.ui.homeScreen

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.NetworkConnectivityManager
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DeviceListDetailViewModel"

@HiltViewModel
class DeviceListDetailViewModel @Inject constructor(
    application: Application,
    private val repository: DeviceRepository,
    private val stateFactory: StateFactory,
    private val preferencesRepository: UserPreferencesRepository,
    networkManager: NetworkConnectivityManager
): AndroidViewModel(application) {
    val isWLEDCaptivePortal = networkManager.isWLEDCaptivePortal

    private var isPolling by mutableStateOf(false)
    private var job: Job? = null

    val showHiddenDevices = preferencesRepository.showHiddenDevices
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val discoveryService = DeviceDiscovery(
        context = getApplication<Application>().applicationContext,
        onDeviceDiscovered = {
            deviceDiscovered(it)
        }
    )

    private val _isAddDeviceBottomSheetVisible = MutableStateFlow(false)
    val isAddDeviceBottomSheetVisible: StateFlow<Boolean> = _isAddDeviceBottomSheetVisible

    fun getDeviceByAddress(address: String): Flow<Device?> {
        Log.d(TAG, "Getting device by address $address")

        if (address == Device.DEFAULT_WLED_AP_IP) {
            return flow {
                emit(Device.getDefaultAPDevice())
            }
        }

        return repository.findLiveDeviceByAddress(address)
    }

    fun startRefreshDevicesLoop() {
        if (isPolling) {
            return
        }
        isPolling = true
        // If there's an existing job that's not registered, kill it.
        job?.cancel()
        Log.i(TAG, "Starting refresh devices loop")
        job = viewModelScope.launch(Dispatchers.IO) {
            while (isPolling && isActive) {
                Log.i(TAG, "Looping refreshes")
                refreshDevices(silent = true)
                delay(10000)
            }
            // If we left the loop, the job either got cancelled or is not active anymore.
            // Let's make sure the state is set correctly.
            stopRefreshDevicesLoop()
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
            Log.d(TAG, "devices found: ${devices.size}")
            for (device in devices) {
                refreshDevice(device, silent)
            }
        }
    }

    private fun refreshDevice(device: Device, silent: Boolean) {
        Log.d(TAG, "Refreshing device ${device.name} - ${device.address}")
        stateFactory.getState(device).requestsManager.addRequest(
            RefreshRequest(
                device,
                silentRefresh = silent,
            )
        )
    }

    private fun startDiscoveryService() {
        Log.i(TAG, "Start device discovery")
        discoveryService.start()
    }

    fun startDiscoveryServiceTimed(timeMillis: Long = 10000) = viewModelScope.launch(Dispatchers.IO) {
        Log.i(TAG, "Start device discovery")
        startDiscoveryService()
        delay(timeMillis)
        stopDiscoveryService()
    }

    fun stopDiscoveryService() {
        Log.i(TAG, "Stop device discovery")
        discoveryService.stop()
    }

    private fun deviceDiscovered(device: Device) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.contains(device)) {
                Log.i(TAG, "Device already exists")
                return@launch
            }
            Log.i(TAG, "IP: ${device.address}\tName: ${device.name}\t")

            val request = RefreshRequest(
                device,
                silentRefresh = true,
                saveChanges = false
            ) { refreshedDevice ->
                if (refreshedDevice.brand != "ILLUMIDEL") {
                    Log.i(TAG, "Device is not Illumidel: ${device.address}")
                    return@RefreshRequest
                }
                val existingDevice = findWithSameMacAddress(refreshedDevice)
                if (existingDevice != null && refreshedDevice.macAddress != Device.UNKNOWN_VALUE) {
                    Log.i(
                        TAG,
                        "Device ${existingDevice.address} already exists with the same mac address ${existingDevice.macAddress}"
                    )
                    val refreshedExistingDevice = existingDevice.copy(
                        address = refreshedDevice.address,
                        isOnline = refreshedDevice.isOnline,
                        name = refreshedDevice.name,
                        brightness = refreshedDevice.brightness,
                        isPoweredOn = refreshedDevice.isPoweredOn,
                        color = refreshedDevice.color,
                        networkRssi = refreshedDevice.networkRssi,
                        isEthernet = refreshedDevice.isEthernet,
                        platformName = refreshedDevice.platformName,
                        version = refreshedDevice.version,
                        brand = refreshedDevice.brand,
                        productName = refreshedDevice.productName,
                    )
                    delete(existingDevice)
                    insert(refreshedExistingDevice)
                } else {
                    insert(refreshedDevice)
                }
            }
            stateFactory.getState(device).requestsManager.addRequest(request)
        }
    }

    private suspend fun findWithSameMacAddress(device: Device): Device? {
        return repository.findDeviceByMacAddress(device.macAddress)
    }

    fun insert(device: Device) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Inserting device ${device.name} - ${device.address}")
        repository.insert(device)
    }

    fun delete(device: Device) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Deleting device ${device.name} - ${device.address}")
        repository.delete(device)
    }

    fun toggleShowHiddenDevices() = viewModelScope.launch(Dispatchers.IO) {
        preferencesRepository.updateShowHiddenDevices(!showHiddenDevices.value)
    }

    fun showAddDeviceBottomSheet() {
        _isAddDeviceBottomSheetVisible.update {
            true
        }
    }
    fun hideAddDeviceBottomSheet() {
        _isAddDeviceBottomSheetVisible.update {
            false
        }
    }
}
