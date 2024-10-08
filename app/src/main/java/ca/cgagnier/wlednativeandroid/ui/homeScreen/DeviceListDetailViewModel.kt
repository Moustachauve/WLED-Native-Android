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
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DeviceListDetailViewModel"

@HiltViewModel
class DeviceListDetailViewModel @Inject constructor(
    application: Application,
    private val repository: DeviceRepository,
    private val stateFactory: StateFactory,
): AndroidViewModel(application) {
    var isPolling by mutableStateOf(false)
        private set
    var isDiscovering by mutableStateOf(false)
        private set
    private var job: Job? = null

    private val discoveryService = DeviceDiscovery(
        context = getApplication<Application>().applicationContext,
        onDeviceDiscovered = {
            deviceDiscovered(it)
        }
    )

    fun getDeviceByAddress(address: String): Flow<Device?> {
        Log.d(TAG, "Getting device by address $address")
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
            Log.d(TAG, "devices found: ${devices.size}")
            devices.map { device ->
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

    fun startDiscoveryService() {
        isDiscovering = true
        discoveryService.start()
    }

    fun stopDiscoveryService() {
        isDiscovering = false
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

    fun insert(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Inserting device ${device.name} - ${device.address}")
        repository.insert(device)
    }

    fun delete(device: Device) = viewModelScope.launch {
        Log.d(TAG, "Deleting device ${device.name} - ${device.address}")
        repository.delete(device)
    }
}
