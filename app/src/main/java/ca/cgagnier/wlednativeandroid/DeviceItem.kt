package ca.cgagnier.wlednativeandroid

data class DeviceItem(
    val name: String,
    val ipAddress: String,
    val brightness: Int,
    val isPoweredOn: Boolean
)
