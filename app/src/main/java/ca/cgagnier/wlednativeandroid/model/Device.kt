package ca.cgagnier.wlednativeandroid.model

import android.graphics.Color
import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val macAddress: String,
    val brightness: Int = 0,
    val color: Int = Color.WHITE,
    val isPoweredOn: Boolean = false,
    val isOnline: Boolean = false,
    val isRefreshing: Boolean = false,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val networkBssid: String = UNKNOWN_VALUE,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val networkRssi: Int = -101,
    @ColumnInfo(defaultValue = "0")
    val networkSignal: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val networkChannel: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val isEthernet: Boolean = false,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val platformName: String = UNKNOWN_VALUE,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val version: String = UNKNOWN_VALUE,
    @ColumnInfo(defaultValue = "")
    val newUpdateVersionTagAvailable: String = "",
    @ColumnInfo(defaultValue = "")
    val skipUpdateTag: String = "",
    @ColumnInfo(defaultValue = "wled")
    val productType: String = "wled",
    @ColumnInfo(defaultValue = "stable")
    val branch: String = "stable",
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

    fun hasUpdateAvailable(): Boolean {
        return newUpdateVersionTagAvailable != ""
    }

    companion object {
        const val UNKNOWN_VALUE = "__unknown__"
    }
}