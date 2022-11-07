package ca.cgagnier.wlednativeandroid.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ca.cgagnier.wlednativeandroid.R

@Entity
data class Device(
    @PrimaryKey
    val address: String,
    val name: String,
    val isCustomName: Boolean,
    val isHidden: Boolean,
    val brightness: Int = 0,
    val color: Int = Color.WHITE,
    val isPoweredOn: Boolean = false,
    val isOnline: Boolean = false,
    val isRefreshing: Boolean = false,
    val networkRssi: Int = -101
) {
    @Ignore
    var isSliding = false

    fun getDeviceUrl(): String {
        return "http://$address"
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