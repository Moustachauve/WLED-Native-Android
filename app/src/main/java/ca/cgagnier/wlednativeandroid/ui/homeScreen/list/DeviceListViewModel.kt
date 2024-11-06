package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.request.StateChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


private const val TAG = "DeviceListViewModel"

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val repository: DeviceRepository,
    private val stateFactory: StateFactory,
    preferencesRepository: UserPreferencesRepository
): ViewModel() {
    private val showHiddenDevices = preferencesRepository.showHiddenDevices
    private val showOfflineDevicesLast = preferencesRepository.showOfflineDevicesLast

    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState
        .combine(showHiddenDevices) { state, showHiddenDevices ->
            state.copy(showHiddenDevices = showHiddenDevices)
        }
        .combine(showOfflineDevicesLast) { state, showOfflineDevicesLast ->
            state.copy(showOfflineDevicesLast = showOfflineDevicesLast)
        }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = DeviceListUiState()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val devices: StateFlow<List<Device>> = uiState.flatMapLatest { state ->
        getDevicesFlow(state)
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = runBlocking {
            getDevicesFlow(uiState.first()).first()
        }
    )

    val shouldShowDevicesAreHidden =
        devices.combine(showHiddenDevices) { devices, showHiddenDevices ->
            if (devices.isEmpty() && !showHiddenDevices) {
                repository.hasHiddenDevices()
            } else {
                false
            }
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = false
        )

    private fun getDevicesFlow(state: DeviceListUiState) : Flow<List<Device>> {
        var devicesFlow =
            if (state.showOfflineDevicesLast) repository.allDevicesOfflineLast else repository.allDevices
        if (!state.showHiddenDevices) {
            devicesFlow = devicesFlow.map { devices ->
                devices.filter { device -> !device.isHidden }
            }
        }
        return devicesFlow
    }

    fun toggleDevicePower(device: Device, isOn: Boolean) {
        val deviceSetPost = JsonPost(isOn = isOn)
        viewModelScope.launch(Dispatchers.IO) {
            stateFactory.getState(device).requestsManager.addRequest(
                StateChangeRequest(device, deviceSetPost)
            )
        }
    }

    fun setDeviceBrightness(device: Device, brightness: Int) {
        val deviceSetPost = JsonPost(brightness = brightness)
        viewModelScope.launch(Dispatchers.IO) {
            stateFactory.getState(device).requestsManager.addRequest(
                StateChangeRequest(device, deviceSetPost)
            )
        }
    }

    fun deleteDevice(device: Device) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Deleting device ${device.name} - ${device.address}")
            repository.delete(device)
        }
    }
}

@Stable
data class DeviceListUiState(
    val showOfflineDevicesLast: Boolean = true,
    val showHiddenDevices: Boolean = false,
    val listState: LazyListState = LazyListState(),
)
