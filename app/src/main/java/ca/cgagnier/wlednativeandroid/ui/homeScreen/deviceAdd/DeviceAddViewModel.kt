package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceAddViewModel @Inject constructor(
    private val repository: DeviceRepository
) : ViewModel() {

    var address by mutableStateOf("")
    var name by mutableStateOf("")
    var isHidden by mutableStateOf(false)

    fun createDevice() = viewModelScope.launch {
        val device = Device(
            address = address,
            name = name,
            isCustomName = name != "",
            isHidden = isHidden,
            macAddress = Device.UNKNOWN_VALUE
        )
        repository.insert(device)
    }
}