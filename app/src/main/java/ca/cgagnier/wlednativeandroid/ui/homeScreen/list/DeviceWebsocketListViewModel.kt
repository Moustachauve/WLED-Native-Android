package ca.cgagnier.wlednativeandroid.ui.homeScreen.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.websocket.DeviceWithState
import ca.cgagnier.wlednativeandroid.service.websocket.WebsocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

private const val TAG = "DeviceWebsocketListViewModel"

@HiltViewModel
class DeviceWebsocketListViewModel @Inject constructor(
    deviceRepository: DeviceRepository
) : ViewModel() {
    private val activeClients = MutableStateFlow<Map<String, WebsocketClient>>(emptyMap())
    private val devicesFromDb = deviceRepository.allDevices

    init {
        viewModelScope.launch {
            devicesFromDb
                .scan(emptyMap<String, WebsocketClient>()) { currentClients, newDeviceList ->
                    // Create a mutable copy of the current client map to build the next state.
                    val nextClients = currentClients.toMutableMap()
                    val newDeviceMap = newDeviceList.associateBy { it.macAddress }

                    // 1. Identify and destroy clients for devices that are no longer present.
                    val devicesToRemove = currentClients.keys - newDeviceMap.keys
                    devicesToRemove.forEach { macAddress ->
                        Log.d(TAG, "[Scan] Device removed: $macAddress. Destroying client.")
                        nextClients[macAddress]?.destroy()
                        nextClients.remove(macAddress)
                    }

                    // 2. Identify and create/update clients for new or changed devices.
                    newDeviceMap.forEach { (macAddress, device) ->
                        val existingClient = currentClients[macAddress]
                        if (existingClient == null) {
                            // Device added: create and connect a new client.
                            Log.d(TAG, "[Scan] Device added: $macAddress. Creating client.")
                            val newClient = WebsocketClient(device)
                            newClient.connect()
                            nextClients[macAddress] = newClient
                        } else if (existingClient.deviceState.device.address != device.address) {
                            // Device IP changed: reconnect the client.
                            Log.d(
                                TAG,
                                "[Scan] Device address changed for $macAddress. Reconnecting client."
                            )
                            existingClient.destroy()
                            val newClient = WebsocketClient(device)
                            newClient.connect()
                            nextClients[macAddress] = newClient
                        }
                    }
                    // Return the updated map, which becomes `currentClients` for the next iteration.
                    nextClients
                }
                .collect { updatedClients ->
                    // Emit the new map of clients to the StateFlow.
                    activeClients.value = updatedClients
                }

        }
    }

    val devicesWithState: StateFlow<List<DeviceWithState>> =
        combine(devicesFromDb, activeClients) { devices, clients ->
            clients.values.map { client ->
                client.deviceState
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared. Closing all WebSocket clients.")
        activeClients.value.values.forEach { it.destroy() }
    }
}