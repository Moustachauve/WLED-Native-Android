package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.domain.use_case.ValidateAddress
import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.StatefulDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceAddViewModel @Inject constructor(
    private val repository: StatefulDeviceRepository,
    private val validateAddress: ValidateAddress
) : ViewModel() {

    var state by mutableStateOf(DeviceAddFormState())

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    fun onEvent(event: DeviceAddFormEvent) {
        when (event) {
            is DeviceAddFormEvent.AddressChanged -> {
                state = state.copy(address = event.address)
            }
            is DeviceAddFormEvent.NameChanged -> {
                state = state.copy(name = event.name)
            }
            is DeviceAddFormEvent.IsHiddenChanged -> {
                state = state.copy(isHidden = event.isHidden)
            }
            is DeviceAddFormEvent.Submit -> {
                submitCreateDevice()
            }
        }
    }

    private fun submitCreateDevice() = viewModelScope.launch(Dispatchers.IO) {
        val emailResult = validateAddress.execute(state.address)
        val hasError = listOf(
            emailResult
        ).any { !it.successful }
        if (hasError) {
            state = state.copy(
                addressError = emailResult.errorMessage
            )
            return@launch
        }

        val trimmedName = state.name.trim()
        val device = StatefulDevice(
            address = state.address.trim(),
            name = trimmedName,
            isCustomName = trimmedName != "",
            isHidden = state.isHidden,
            macAddress = StatefulDevice.UNKNOWN_VALUE
        )
        repository.insert(device)
        validationEventChannel.send(ValidationEvent.Success)
    }

    fun clear() {
        state = DeviceAddFormState()
    }

    sealed class ValidationEvent {
        data object Success: ValidationEvent()
    }
}