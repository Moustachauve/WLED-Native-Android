package ca.cgagnier.wlednativeandroid

data class DeviceListItem(
    val name: String,
    val ipAddress: String,
    val brightness: Int,
    val isPoweredOn: Boolean
)
