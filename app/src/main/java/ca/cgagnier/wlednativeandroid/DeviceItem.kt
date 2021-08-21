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
    val isOnline: Boolean = false,

    @Transient
    val isRefreshing: Boolean = false,

    @Transient
    val networkRssi: Int = -101
) {

    fun getDeviceUrl(): String {
        return "http://$address"
    }

    fun isSame(device: DeviceItem): Boolean {
        return isSameForSave(device)
                && brightness == device.brightness
                && isPoweredOn == device.isPoweredOn
                && isOnline == device.isOnline
                && isRefreshing == device.isRefreshing
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

    fun getNetworkStrengthImage(): Int {
        if (!isOnline) {
            return R.drawable.twotone_signal_wifi_connected_no_internet_0_24
        }
        if (networkRssi >= -50) {
            return R.drawable.twotone_signal_wifi_4_bar_24
        }
        if (networkRssi >= -70) {
            return R.drawable.twotone_signal_wifi_3_bar_24
        }
        if (networkRssi >= -80) {
            return R.drawable.twotone_signal_wifi_2_bar_24
        }
        if (networkRssi >= -100) {
            return R.drawable.twotone_signal_wifi_1_bar_24
        }
        return R.drawable.twotone_signal_wifi_0_bar_24
    }
}
