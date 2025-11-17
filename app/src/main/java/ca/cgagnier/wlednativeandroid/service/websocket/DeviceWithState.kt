package ca.cgagnier.wlednativeandroid.service.websocket

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo

class DeviceWithState(val device: Device) {
    val stateInfo: MutableState<DeviceStateInfo?> = mutableStateOf(null)
    val isWebsocketConnected: MutableState<Boolean> = mutableStateOf(false)

    fun displayName(): String {
        // TODO: copied from WLED-Desktop, but UI logic should probably not be mixed
        //  in the business logic (or whatever this is called)
        return device.customName ?: device.originalName ?: "(New Device)"
    }
}