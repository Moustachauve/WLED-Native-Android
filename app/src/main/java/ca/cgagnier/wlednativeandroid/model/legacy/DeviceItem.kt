package ca.cgagnier.wlednativeandroid.model.legacy

import android.graphics.Color
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Deprecated("", ReplaceWith("ca.cgagnier.wlednativeandroid.model.Device"))
@JsonClass(generateAdapter = true)
data class DeviceItem(
    @Json(name = "address")
    val address: String,

    @Json(name = "name")
    val name: String = "",

    @Json(name = "customName")
    val isCustomName: Boolean = false,

    @Json(name = "hidden")
    val isHidden: Boolean = false,

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
)
