package ca.cgagnier.wlednativeandroid

import android.graphics.Color
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceItem(
    @Json(name = "address")
    val address: String,

    @Json(name = "name")
    val name: String = "",

    @Json(name = "customName")
    val isCustomName: Boolean = false,

    @Transient
    val brightness: Int = 0,

    @Transient
    val color: Int = Color.WHITE,

    @Transient
    val isPoweredOn: Boolean = false,

    @Transient
    val isOnline: Boolean = false
) {

    fun getDeviceUrl(): String {
        return "http://$address"
    }

    fun isSame(device: DeviceItem): Boolean {
        return isSameForSave(device)
                && brightness == device.brightness
                && isPoweredOn == device.isPoweredOn
                && isOnline == device.isOnline
    }

    fun isSameForSave(device: DeviceItem): Boolean {
        return address == device.address
                && name == device.name
                && isCustomName == device.isCustomName
    }

    override fun equals(other: Any?): Boolean {
        if (other is DeviceItem) {
            return other.address == address
        }
        return super.equals(other)
    }
}
