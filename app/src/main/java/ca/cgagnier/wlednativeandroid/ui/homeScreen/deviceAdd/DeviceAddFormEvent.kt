package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd

sealed class DeviceAddFormEvent {
    data class AddressChanged(val address: String) : DeviceAddFormEvent()
    data class NameChanged(val name: String) : DeviceAddFormEvent()
    data class IsHiddenChanged(val isHidden: Boolean) : DeviceAddFormEvent()
    data object Submit : DeviceAddFormEvent()
}