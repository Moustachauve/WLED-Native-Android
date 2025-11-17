package ca.cgagnier.wlednativeandroid.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a stateless WLED device
 */
@Entity(tableName = "device2")
@Parcelize
data class Device(
    @PrimaryKey
    val macAddress: String,
    val address: String,
    val isHidden: Boolean,
    val originalName: String?,
    val customName: String?,
): Parcelable {

    fun getDeviceUrl(): String {
        return "http://$address"
    }
}