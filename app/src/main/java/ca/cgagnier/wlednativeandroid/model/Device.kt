package ca.cgagnier.wlednativeandroid.model

import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ca.cgagnier.wlednativeandroid.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
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
    @ColumnInfo(defaultValue = "UNKNOWN")
    val branch: Branch = Branch.UNKNOWN,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val brand: String = UNKNOWN_VALUE,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val productName: String = UNKNOWN_VALUE,
    @ColumnInfo(defaultValue = UNKNOWN_VALUE)
    val release: String = UNKNOWN_VALUE,
): Parcelable {
    @Ignore
    @IgnoredOnParcel
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

    fun isAPMode(): Boolean {
        return address == DEFAULT_WLED_AP_IP
    }

    companion object {
        const val UNKNOWN_VALUE = "__unknown__"
        const val DEFAULT_WLED_AP_IP = "4.3.2.1"

        fun getDefaultAPDevice(): Device {
            return Device(
                address = DEFAULT_WLED_AP_IP,
                name = "WLED AP Mode",
                isCustomName = true,
                isHidden = false,
                isOnline = true,
                networkRssi = 1,
                macAddress = UNKNOWN_VALUE
            )
        }

        fun getPreviewDevice(): Device {
            return Device(
                "10.0.0.1",
                "Preview Device",
                isCustomName = false,
                isHidden = false,
                macAddress = "00:00:00:00:00:00"
            )
        }
    }
}