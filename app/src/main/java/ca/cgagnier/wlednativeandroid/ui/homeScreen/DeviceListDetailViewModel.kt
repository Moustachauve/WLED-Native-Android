package ca.cgagnier.wlednativeandroid.ui.homeScreen

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.StatefulDeviceRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.NetworkConnectivityManager
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.RefreshRequest
import ca.cgagnier.wlednativeandroid.service.websocket.DeviceWithState
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
    private val repository: StatefulDeviceRepository,
    private val stateFactory: StateFactory,
    private val preferencesRepository: UserPreferencesRepository,
    networkManager: NetworkConnectivityManager
): AndroidViewModel(application) {
    val isWLEDCaptivePortal = networkManager.isWLEDCaptivePortal

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

    private fun deviceDiscovered(device: StatefulDevice) {
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
                if (existingDevice != null && refreshedDevice.macAddress != StatefulDevice.UNKNOWN_VALUE) {
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

    private suspend fun findWithSameMacAddress(device: StatefulDevice): StatefulDevice? {
        return repository.findDeviceByMacAddress(device.macAddress)
    }

    fun insert(device: StatefulDevice) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Inserting device ${device.name} - ${device.address}")
        repository.insert(device)
    }

    fun delete(device: StatefulDevice) = viewModelScope.launch(Dispatchers.IO) {
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
